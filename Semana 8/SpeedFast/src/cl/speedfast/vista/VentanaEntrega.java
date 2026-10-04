package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;
import cl.speedfast.modelo.Entrega;
import cl.speedfast.modelo.EstadoPedido;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.Repartidor;
import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona entregas, filtros, finalización y correcciones de historial.
 */
public class VentanaEntrega extends JFrame {

    private final ControladorDeEnvios controlador;
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final EntregaDAO entregaDAO = new EntregaDAO();

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private JComboBox<Pedido> cmbPedidos;
    private JComboBox<Repartidor> cmbRepartidores;

    private JTable tablaEntregas;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> ordenadorTabla;

    private JComboBox<Object> cmbFiltroPedido;
    private JComboBox<Object> cmbFiltroRepartidor;
    private final JLabel lblResumen = new JLabel();

    private Pedido pedidoAsignado;
    private Repartidor repartidorAsignado;

    private boolean operacionEnCurso;
    private boolean actualizandoFiltros;

    public VentanaEntrega(ControladorDeEnvios controlador) {
        this.controlador = controlador;

        configurarVentana();
        crearComponentes();
        refrescarDatos();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                Window anterior = e.getOppositeWindow();

                if (anterior instanceof Dialog
                        && anterior.getOwner() == VentanaEntrega.this) {
                    return;
                }

                if (!operacionEnCurso) {
                    refrescarDatos();
                }
            }
        });
    }

    private void configurarVentana() {
        setTitle("SpeedFast - Gestión de Entregas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setMinimumSize(new Dimension(950, 550));
        setLocationRelativeTo(null);
    }

    private void crearComponentes() {
        JPanel principal = new JPanel(new BorderLayout(10, 15));
        principal.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel(
                "GESTIÓN DE ENTREGAS",
                SwingConstants.CENTER
        );
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        cmbPedidos = new JComboBox<>();
        cmbRepartidores = new JComboBox<>();

        JPanel formulario =
                new JPanel(new GridLayout(2, 1, 10, 10));

        formulario.add(crearFila("Pedido:", cmbPedidos));
        formulario.add(crearFila("Repartidor:", cmbRepartidores));

        JPanel superior = new JPanel(new BorderLayout(10, 15));
        superior.add(titulo, BorderLayout.NORTH);
        superior.add(formulario, BorderLayout.CENTER);

        modeloTabla = new DefaultTableModel(
                new Object[]{
                        "ID", "Pedido", "Dirección",
                        "Repartidor", "Nombre",
                        "Fecha", "Hora", "Estado pedido"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 || columna == 1 || columna == 3
                        ? Integer.class
                        : String.class;
            }
        };

        tablaEntregas = new JTable(modeloTabla);
        tablaEntregas.setRowHeight(25);

        ordenadorTabla = new TableRowSorter<>(modeloTabla);
        tablaEntregas.setRowSorter(ordenadorTabla);
        ordenadorTabla.addRowSorterListener(e -> actualizarResumen());

        tablaEntregas.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tablaEntregas.getTableHeader().setReorderingAllowed(false);
        tablaEntregas.getColumnModel().getColumn(0).setMaxWidth(60);
        tablaEntregas.getColumnModel()
                .getColumn(2).setPreferredWidth(220);

        JPanel botones = new JPanel(new GridLayout(2, 4, 8, 8));

        JButton btnAsignar = new JButton("Asignar repartidor");
        JButton btnIniciar = new JButton("Iniciar entrega");
        JButton btnCompletar = new JButton("Marcar entregado");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnVolver = new JButton("Volver");

        botones.add(btnAsignar);
        botones.add(btnIniciar);
        botones.add(btnCompletar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnRefrescar);
        botones.add(btnVolver);

        btnAsignar.addActionListener(
                e -> ejecutarOperacion(this::asignarRepartidor)
        );
        btnIniciar.addActionListener(
                e -> ejecutarOperacion(this::iniciarEntrega)
        );
        btnCompletar.addActionListener(
                e -> ejecutarOperacion(this::marcarEntregado)
        );
        btnEditar.addActionListener(
                e -> ejecutarOperacion(this::editarEntrega)
        );
        btnEliminar.addActionListener(
                e -> ejecutarOperacion(this::eliminarEntrega)
        );
        btnRefrescar.addActionListener(
                e -> ejecutarOperacion(this::refrescarDatos)
        );
        btnVolver.addActionListener(e -> dispose());

        cmbPedidos.addActionListener(e -> limpiarAsignacion());
        cmbRepartidores.addActionListener(e -> limpiarAsignacion());

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.add(crearPanelFiltros(), BorderLayout.NORTH);
        centro.add(
                new JScrollPane(tablaEntregas),
                BorderLayout.CENTER
        );

        principal.add(superior, BorderLayout.NORTH);
        principal.add(centro, BorderLayout.CENTER);
        principal.add(botones, BorderLayout.SOUTH);

        add(principal);
    }

    /**
     * Los filtros consultan la tabla y son independientes de la asignación.
     */
    private JPanel crearPanelFiltros() {
        cmbFiltroPedido = new JComboBox<>();
        cmbFiltroRepartidor = new JComboBox<>();

        cmbFiltroPedido.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> lista,
                    Object valor,
                    int indice,
                    boolean seleccionado,
                    boolean tieneFoco
            ) {
                Object texto = valor instanceof Pedido pedido
                        ? pedido.getIdPedido() + " - "
                          + pedido.getDireccionEntrega()
                        : valor;

                return super.getListCellRendererComponent(
                        lista,
                        texto,
                        indice,
                        seleccionado,
                        tieneFoco
                );
            }
        });

        JButton btnMostrarTodas = new JButton("Mostrar todas");

        btnMostrarTodas.addActionListener(e -> {
            actualizandoFiltros = true;

            try {
                if (cmbFiltroPedido.getItemCount() > 0) {
                    cmbFiltroPedido.setSelectedIndex(0);
                }

                if (cmbFiltroRepartidor.getItemCount() > 0) {
                    cmbFiltroRepartidor.setSelectedIndex(0);
                }

            } finally {
                actualizandoFiltros = false;
            }

            aplicarFiltros();
        });

        cmbFiltroPedido.addActionListener(e -> aplicarFiltros());
        cmbFiltroRepartidor.addActionListener(e -> aplicarFiltros());

        JPanel campos = new JPanel(new GridLayout(1, 2, 15, 0));
        campos.add(crearFila("Pedido:", cmbFiltroPedido));
        campos.add(crearFila("Repartidor:", cmbFiltroRepartidor));

        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setBorder(
                BorderFactory.createTitledBorder("Consultar entregas")
        );
        panel.add(campos, BorderLayout.CENTER);
        panel.add(btnMostrarTodas, BorderLayout.EAST);
        panel.add(lblResumen, BorderLayout.SOUTH);

        return panel;
    }

    private Integer obtenerIdFiltroPedido() {
        return cmbFiltroPedido.getSelectedItem() instanceof Pedido pedido
                ? pedido.getIdPedido()
                : null;
    }

    private Integer obtenerIdFiltroRepartidor() {
        return cmbFiltroRepartidor.getSelectedItem()
                instanceof Repartidor repartidor
                ? repartidor.getIdRepartidor()
                : null;
    }

    /**
     * Actualiza los filtros conservando la selección por ID.
     */
    private void cargarFiltros(
            List<Pedido> pedidos,
            List<Repartidor> repartidores
    ) {
        Integer idPedido = obtenerIdFiltroPedido();
        Integer idRepartidor = obtenerIdFiltroRepartidor();

        actualizandoFiltros = true;

        try {
            cmbFiltroPedido.removeAllItems();
            cmbFiltroRepartidor.removeAllItems();

            cmbFiltroPedido.addItem("Todos los pedidos");
            cmbFiltroRepartidor.addItem("Todos los repartidores");

            for (Pedido pedido : pedidos) {
                cmbFiltroPedido.addItem(pedido);

                if (Integer.valueOf(pedido.getIdPedido()).equals(idPedido)) {
                    cmbFiltroPedido.setSelectedItem(pedido);
                }
            }

            for (Repartidor repartidor : repartidores) {
                cmbFiltroRepartidor.addItem(repartidor);

                if (Integer.valueOf(repartidor.getIdRepartidor())
                        .equals(idRepartidor)) {

                    cmbFiltroRepartidor.setSelectedItem(repartidor);
                }
            }

        } finally {
            actualizandoFiltros = false;
        }

        aplicarFiltros();
    }

    private void aplicarFiltros() {
        if (actualizandoFiltros) {
            return;
        }

        tablaEntregas.clearSelection();

        ordenadorTabla.setRowFilter(
                crearFiltro(
                        obtenerIdFiltroPedido(),
                        obtenerIdFiltroRepartidor()
                )
        );

        actualizarResumen();
    }

    /**
     * Compara IDs exactos y combina los filtros mediante AND.
     */
    private static RowFilter<DefaultTableModel, Integer> crearFiltro(
            Integer idPedido,
            Integer idRepartidor
    ) {
        if (idPedido == null && idRepartidor == null) {
            return null;
        }

        return new RowFilter<>() {
            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel,
                            ? extends Integer> fila
            ) {
                int pedido = ((Number) fila.getValue(1)).intValue();
                int repartidor = ((Number) fila.getValue(3)).intValue();

                return (idPedido == null
                        || idPedido.intValue() == pedido)
                        && (idRepartidor == null
                        || idRepartidor.intValue() == repartidor);
            }
        };
    }

    private void actualizarResumen() {
        lblResumen.setText(
                "Entregas mostradas: " + tablaEntregas.getRowCount()
                        + " de " + modeloTabla.getRowCount()
        );
    }

    private JPanel crearFila(String texto, JComponent componente) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setPreferredSize(new Dimension(100, 25));

        fila.add(etiqueta, BorderLayout.WEST);
        fila.add(componente, BorderLayout.CENTER);

        return fila;
    }

    private void ejecutarOperacion(Runnable operacion) {
        operacionEnCurso = true;

        try {
            operacion.run();

        } finally {
            operacionEnCurso = false;
        }
    }

    private void refrescarDatos() {
        try {
            List<Pedido> pedidos = pedidoDAO.readAll();
            List<Repartidor> repartidores = repartidorDAO.readAll();
            List<Entrega> entregas = entregaDAO.readAll();

            Map<Integer, Pedido> mapaPedidos = new HashMap<>();
            Map<Integer, Repartidor> mapaRepartidores = new HashMap<>();

            limpiarAsignacion();
            cmbPedidos.removeAllItems();
            cmbRepartidores.removeAllItems();

            for (Pedido pedido : pedidos) {
                cmbPedidos.addItem(pedido);
                mapaPedidos.put(pedido.getIdPedido(), pedido);
            }

            for (Repartidor repartidor : repartidores) {
                cmbRepartidores.addItem(repartidor);
                mapaRepartidores.put(
                        repartidor.getIdRepartidor(),
                        repartidor
                );
            }

            modeloTabla.setRowCount(0);

            for (Entrega entrega : entregas) {
                Pedido pedido =
                        mapaPedidos.get(entrega.getIdPedido());

                Repartidor repartidor =
                        mapaRepartidores.get(entrega.getIdRepartidor());

                modeloTabla.addRow(new Object[]{
                        entrega.getIdEntrega(),
                        entrega.getIdPedido(),
                        pedido == null
                                ? "No disponible"
                                : pedido.getDireccionEntrega(),
                        entrega.getIdRepartidor(),
                        repartidor == null
                                ? "No disponible"
                                : repartidor.getNombre(),
                        entrega.getFecha().toString(),
                        entrega.getHora().format(formatoHora),
                        pedido == null
                                ? "No disponible"
                                : pedido.getEstado().name()
                });
            }

            cargarFiltros(pedidos, repartidores);

        } catch (Exception e) {
            mostrarError("cargar los datos de entregas", e);
        }
    }

    private void limpiarAsignacion() {
        pedidoAsignado = null;
        repartidorAsignado = null;
    }

    private void asignarRepartidor() {
        Pedido pedido =
                (Pedido) cmbPedidos.getSelectedItem();

        Repartidor repartidor =
                (Repartidor) cmbRepartidores.getSelectedItem();

        if (pedido == null || repartidor == null) {
            mostrarAdvertencia(
                    "Debe seleccionar un pedido y un repartidor."
            );
            return;
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            mostrarAdvertencia(
                    "Solo puede iniciar entregas de pedidos PENDIENTES."
            );
            return;
        }

        pedido.asignarRepartidor(repartidor.getNombre());
        pedidoAsignado = pedido;
        repartidorAsignado = repartidor;

        mostrarExito(
                "Repartidor " + repartidor.getNombre()
                        + " asignado al pedido #" + pedido.getIdPedido()
                        + ".\nPresione Iniciar entrega para guardar."
        );
    }

    private void iniciarEntrega() {
        Pedido pedido =
                (Pedido) cmbPedidos.getSelectedItem();

        if (pedido == null
                || pedidoAsignado != pedido
                || repartidorAsignado == null) {

            mostrarAdvertencia(
                    "Debe asignar un repartidor antes de iniciar la entrega."
            );
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();

        Entrega entrega = new Entrega(
                pedido.getIdPedido(),
                repartidorAsignado.getIdRepartidor(),
                ahora.toLocalDate(),
                ahora.toLocalTime().withNano(0)
        );

        try {
            if (entregaDAO.create(entrega)) {
                controlador.reservarPedido(pedido);
                controlador.despacharPedido(pedido);
                pedido.setEstado(EstadoPedido.EN_REPARTO);

                mostrarExito(
                        "Entrega registrada con ID "
                                + entrega.getIdEntrega()
                                + ".\nEl pedido #"
                                + pedido.getIdPedido()
                                + " está EN_REPARTO."
                );

                refrescarDatos();
            }

        } catch (Exception e) {
            mostrarError("iniciar la entrega", e);
        }
    }

    private void marcarEntregado() {
        int fila = obtenerFilaSeleccionada();

        if (fila < 0) {
            return;
        }

        int idEntrega =
                (Integer) modeloTabla.getValueAt(fila, 0);

        int idPedido =
                (Integer) modeloTabla.getValueAt(fila, 1);

        String estado =
                (String) modeloTabla.getValueAt(fila, 7);

        if (!EstadoPedido.EN_REPARTO.name().equals(estado)) {
            mostrarAdvertencia(
                    "Solo puede completar pedidos que estén EN_REPARTO."
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Marcar el pedido #" + idPedido + " como ENTREGADO?",
                "Confirmar entrega",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (pedidoDAO.marcarEntregado(idEntrega)) {
                mostrarExito(
                        "Pedido marcado como ENTREGADO correctamente."
                );

            } else {
                mostrarAdvertencia(
                        "La entrega ya no existe o el pedido "
                                + "ya no está EN_REPARTO."
                );
            }

            refrescarDatos();

        } catch (Exception e) {
            mostrarError("completar la entrega", e);
        }
    }

    private int obtenerFilaSeleccionada() {
        int fila = tablaEntregas.getSelectedRow();

        if (fila < 0) {
            mostrarAdvertencia(
                    "Seleccione una entrega de la tabla."
            );
            return -1;
        }

        return tablaEntregas.convertRowIndexToModel(fila);
    }

    private void editarEntrega() {
        int fila = obtenerFilaSeleccionada();

        if (fila < 0) {
            return;
        }

        int idEntrega =
                (Integer) modeloTabla.getValueAt(fila, 0);

        int idPedido =
                (Integer) modeloTabla.getValueAt(fila, 1);

        int idRepartidor =
                (Integer) modeloTabla.getValueAt(fila, 3);

        try {
            List<Pedido> pedidosActuales = pedidoDAO.readAll();

            JComboBox<Pedido> comboPedido = new JComboBox<>(
                    pedidosActuales.toArray(new Pedido[0])
            );

            JComboBox<Repartidor> comboRepartidor = new JComboBox<>(
                    repartidorDAO.readAll().toArray(new Repartidor[0])
            );

            boolean entregaCompletada = false;

            comboPedido.setSelectedIndex(-1);
            comboRepartidor.setSelectedIndex(-1);

            for (int i = 0; i < comboPedido.getItemCount(); i++) {
                if (comboPedido.getItemAt(i).getIdPedido() == idPedido) {
                    comboPedido.setSelectedIndex(i);

                    entregaCompletada =
                            comboPedido.getItemAt(i).getEstado()
                                    == EstadoPedido.ENTREGADO;
                    break;
                }
            }

            for (int i = 0; i < comboRepartidor.getItemCount(); i++) {
                if (comboRepartidor.getItemAt(i).getIdRepartidor()
                        == idRepartidor) {

                    comboRepartidor.setSelectedIndex(i);
                    break;
                }
            }

            JTextField txtFecha = new JTextField(
                    (String) modeloTabla.getValueAt(fila, 5)
            );

            JTextField txtHora = new JTextField(
                    (String) modeloTabla.getValueAt(fila, 6)
            );

            JPanel formulario =
                    new JPanel(new GridLayout(4, 1, 10, 10));

            formulario.setPreferredSize(new Dimension(650, 160));

            formulario.add(crearFila("Pedido:", comboPedido));
            formulario.add(crearFila("Repartidor:", comboRepartidor));
            formulario.add(crearFila("Fecha:", txtFecha));
            formulario.add(crearFila("Hora:", txtHora));

            txtFecha.setToolTipText("Formato: AAAA-MM-DD");
            txtHora.setToolTipText("Formato: HH:mm:ss");

            while (true) {
                int respuesta = JOptionPane.showConfirmDialog(
                        this,
                        formulario,
                        "Editar entrega #" + idEntrega
                                + " | Fecha: AAAA-MM-DD | Hora: HH:mm:ss",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (respuesta != JOptionPane.OK_OPTION) {
                    return;
                }

                Pedido pedido =
                        (Pedido) comboPedido.getSelectedItem();

                Repartidor repartidor =
                        (Repartidor) comboRepartidor.getSelectedItem();

                if (pedido == null || repartidor == null) {
                    mostrarAdvertencia(
                            "Debe seleccionar un pedido y un repartidor."
                    );
                    continue;
                }

                String fechaTexto = txtFecha.getText().trim();
                String horaTexto = txtHora.getText().trim();

                if (!fechaTexto.matches("\\d{4}-\\d{2}-\\d{2}")
                        || !horaTexto.matches("\\d{2}:\\d{2}:\\d{2}")) {

                    mostrarAdvertencia(
                            "Use fecha AAAA-MM-DD y hora HH:mm:ss.\n"
                                    + "Ejemplo: 2026-10-04 y 15:30:00."
                    );
                    continue;
                }

                LocalDate fecha;
                LocalTime hora;

                try {
                    fecha = LocalDate.parse(fechaTexto);
                    hora = LocalTime.parse(horaTexto);

                } catch (DateTimeParseException e) {
                    mostrarAdvertencia(
                            "La fecha o la hora no son válidas."
                    );
                    continue;
                }

                Entrega entrega = new Entrega(
                        pedido.getIdPedido(),
                        repartidor.getIdRepartidor(),
                        fecha,
                        hora
                );

                entrega.setIdEntrega(idEntrega);

                if (entregaCompletada && !confirmarCorreccion()) {
                    continue;
                }

                try {
                    if (entregaDAO.update(entrega)) {
                        mostrarExito(
                                "Entrega actualizada correctamente."
                        );

                    } else {
                        mostrarAdvertencia(
                                "La entrega ya no existe en la base de datos."
                        );
                    }

                    refrescarDatos();
                    return;

                } catch (IllegalArgumentException e) {
                    mostrarAdvertencia(e.getMessage());
                }
            }

        } catch (Exception e) {
            mostrarError("editar la entrega", e);
        }
    }

    private void eliminarEntrega() {
        int fila = obtenerFilaSeleccionada();

        if (fila < 0) {
            return;
        }

        int idEntrega =
                (Integer) modeloTabla.getValueAt(fila, 0);

        boolean completada = EstadoPedido.ENTREGADO.name().equals(
                modeloTabla.getValueAt(fila, 7)
        );

        String mensaje =
                "¿Desea eliminar la entrega #" + idEntrega + "?\n";

        mensaje += completada
                ? "Está corrigiendo el historial de una entrega completada.\n"
                  + "Se eliminará su registro de entrega; "
                  + "el pedido conservará el estado ENTREGADO."
                : "Si el pedido estaba en reparto y queda "
                  + "sin entregas, volverá a PENDIENTE.";

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                mensaje,
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (entregaDAO.delete(idEntrega)) {
                mostrarExito(
                        "Entrega eliminada correctamente."
                );

            } else {
                mostrarAdvertencia(
                        "La entrega ya no existe en la base de datos."
                );
            }

            refrescarDatos();

        } catch (Exception e) {
            mostrarError("eliminar la entrega", e);
        }
    }

    private boolean confirmarCorreccion() {
        return JOptionPane.showConfirmDialog(
                this,
                "Esta entrega ya fue completada.\n"
                        + "Modificarla corregirá su historial.\n"
                        + "Si cambia de pedido, el original "
                        + "conservará su estado ENTREGADO.\n"
                        + "¿Desea guardar la corrección?",
                "Corregir entrega completada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    private void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Operación exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Aviso",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarError(String operacion, Exception error) {
        if (error instanceof IllegalArgumentException) {
            mostrarAdvertencia(error.getMessage());
            return;
        }

        error.printStackTrace();

        String mensaje =
                error instanceof SQLIntegrityConstraintViolationException
                        ? "El pedido o el repartidor ya no están disponibles.\n"
                          + "Refresque los datos y vuelva a intentarlo."
                        : "No fue posible " + operacion + ".\n"
                          + "Compruebe la conexión y disponibilidad de MySQL.";

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );
    }
}