package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;
import cl.speedfast.modelo.EstadoPedido;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.PedidoComida;
import cl.speedfast.modelo.PedidoEncomienda;
import cl.speedfast.modelo.PedidoExpress;
import dao.PedidoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Lista los pedidos persistidos y permite editarlos o eliminarlos.
 */
public class VentanaListaPedidos extends JFrame {

    private final PedidoDAO pedidoDAO;

    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private Timer timerActualizacion;

    public VentanaListaPedidos(ControladorDeEnvios controlador) {
        pedidoDAO = new PedidoDAO();

        configurarVentana();
        crearComponentes();

        timerActualizacion = new Timer(
                5000,
                e -> cargarPedidos()
        );

        if (cargarPedidos()) {
            timerActualizacion.start();
        }
    }

    private void configurarVentana() {
        setTitle("SpeedFast - Gestión de Pedidos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 450);
        setMinimumSize(new Dimension(750, 400));
        setLocationRelativeTo(null);
    }

    private void crearComponentes() {
        JPanel principal = new JPanel(new BorderLayout(10, 15));
        principal.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel(
                "GESTIÓN DE PEDIDOS",
                SwingConstants.CENTER
        );
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Tipo", "Dirección", "Estado"},
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                return columna == 0 ? Integer.class : String.class;
            }
        };

        tablaPedidos = new JTable(modeloTabla);
        tablaPedidos.setRowHeight(25);
        tablaPedidos.setAutoCreateRowSorter(true);
        tablaPedidos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tablaPedidos.getTableHeader().setReorderingAllowed(false);
        tablaPedidos.getColumnModel().getColumn(0).setMaxWidth(80);
        tablaPedidos.getColumnModel()
                .getColumn(2).setPreferredWidth(350);

        JPanel botones = new JPanel(new GridLayout(1, 4, 10, 0));

        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnVolver = new JButton("Volver");

        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnRefrescar);
        botones.add(btnVolver);

        btnEditar.addActionListener(e -> editarPedido());
        btnEliminar.addActionListener(e -> eliminarPedido());

        btnRefrescar.addActionListener(e -> {
            if (cargarPedidos()) {
                timerActualizacion.start();
            }
        });

        btnVolver.addActionListener(e -> dispose());

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);
        principal.add(botones, BorderLayout.SOUTH);

        add(principal);
    }

    /**
     * Actualiza la tabla y conserva la selección por ID.
     */
    private boolean cargarPedidos() {
        Integer idSeleccionado = null;
        int filaSeleccionada = tablaPedidos.getSelectedRow();

        if (filaSeleccionada >= 0) {
            int filaModelo = tablaPedidos.convertRowIndexToModel(
                    filaSeleccionada
            );

            idSeleccionado =
                    (Integer) modeloTabla.getValueAt(filaModelo, 0);
        }

        try {
            List<Pedido> pedidos = pedidoDAO.readAll();
            modeloTabla.setRowCount(0);

            for (Pedido pedido : pedidos) {
                modeloTabla.addRow(new Object[]{
                        pedido.getIdPedido(),
                        pedidoDAO.obtenerTipoPedido(pedido),
                        pedido.getDireccionEntrega(),
                        pedido.getEstado().name()
                });
            }

            if (idSeleccionado != null) {
                for (int fila = 0;
                     fila < modeloTabla.getRowCount();
                     fila++) {

                    if (idSeleccionado.equals(
                            modeloTabla.getValueAt(fila, 0)
                    )) {
                        int filaVista =
                                tablaPedidos.convertRowIndexToView(fila);

                        if (filaVista >= 0) {
                            tablaPedidos.setRowSelectionInterval(
                                    filaVista,
                                    filaVista
                            );
                        }

                        break;
                    }
                }
            }

            return true;

        } catch (SQLException e) {
            detenerActualizacion();
            mostrarErrorSQL("cargar los pedidos", e);

        } catch (IllegalArgumentException e) {
            detenerActualizacion();
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Hay un pedido con tipo o estado inválido "
                            + "en la base de datos.",
                    "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        return false;
    }

    private int obtenerFilaSeleccionada() {
        int fila = tablaPedidos.getSelectedRow();

        if (fila < 0) {
            mostrarAdvertencia(
                    "Seleccione un pedido de la tabla."
            );
            return -1;
        }

        return tablaPedidos.convertRowIndexToModel(fila);
    }

    private void editarPedido() {
        int fila = obtenerFilaSeleccionada();

        if (fila < 0) {
            return;
        }

        int idPedido =
                (Integer) modeloTabla.getValueAt(fila, 0);

        JTextField txtDireccion = new JTextField(
                (String) modeloTabla.getValueAt(fila, 2)
        );

        JComboBox<String> cmbTipo = new JComboBox<>(
                new String[]{"COMIDA", "ENCOMIENDA", "EXPRESS"}
        );
        cmbTipo.setSelectedItem(
                modeloTabla.getValueAt(fila, 1)
        );

        JComboBox<EstadoPedido> cmbEstado =
                new JComboBox<>(EstadoPedido.values());

        cmbEstado.setSelectedItem(
                EstadoPedido.valueOf(
                        (String) modeloTabla.getValueAt(fila, 3)
                )
        );

        JPanel formulario =
                new JPanel(new GridLayout(3, 2, 10, 10));

        formulario.add(new JLabel("Dirección:"));
        formulario.add(txtDireccion);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(cmbTipo);
        formulario.add(new JLabel("Estado:"));
        formulario.add(cmbEstado);

        boolean estabaActivo = timerActualizacion.isRunning();
        detenerActualizacion();

        try {
            while (true) {
                int respuesta = JOptionPane.showConfirmDialog(
                        this,
                        formulario,
                        "Editar pedido #" + idPedido,
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (respuesta != JOptionPane.OK_OPTION) {
                    return;
                }

                String direccion = txtDireccion.getText().trim();

                if (direccion.isEmpty()) {
                    mostrarAdvertencia(
                            "Debe ingresar la dirección de entrega."
                    );
                    continue;
                }

                if (direccion.length() > 150) {
                    mostrarAdvertencia(
                            "La dirección no puede superar "
                                    + "los 150 caracteres."
                    );
                    continue;
                }

                String tipo =
                        (String) cmbTipo.getSelectedItem();

                EstadoPedido estado =
                        (EstadoPedido) cmbEstado.getSelectedItem();

                Pedido pedido = switch (tipo) {
                    case "COMIDA" ->
                            new PedidoComida(idPedido, direccion, 0);

                    case "ENCOMIENDA" ->
                            new PedidoEncomienda(idPedido, direccion, 0);

                    case "EXPRESS" ->
                            new PedidoExpress(idPedido, direccion, 0);

                    default -> throw new IllegalArgumentException(
                            "Tipo de pedido no válido."
                    );
                };

                pedido.setEstado(estado);

                try {
                    if (pedidoDAO.update(pedido)) {
                        mostrarExito(
                                "Pedido actualizado correctamente."
                        );

                    } else {
                        mostrarAdvertencia(
                                "El pedido ya no existe en la base de datos."
                        );
                    }

                    estabaActivo = cargarPedidos() && estabaActivo;
                    return;

                } catch (IllegalArgumentException e) {
                    mostrarAdvertencia(e.getMessage());
                }
            }

        } catch (SQLException e) {
            mostrarErrorSQL("editar el pedido", e);

        } finally {
            if (estabaActivo && isDisplayable()) {
                timerActualizacion.start();
            }
        }
    }

    private void eliminarPedido() {
        int fila = obtenerFilaSeleccionada();

        if (fila < 0) {
            return;
        }

        int idPedido =
                (Integer) modeloTabla.getValueAt(fila, 0);

        boolean estabaActivo = timerActualizacion.isRunning();
        detenerActualizacion();

        try {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea eliminar el pedido #" + idPedido + "?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }

            if (pedidoDAO.delete(idPedido)) {
                mostrarExito(
                        "Pedido eliminado correctamente."
                );

            } else {
                mostrarAdvertencia(
                        "El pedido ya no existe en la base de datos."
                );
            }

            estabaActivo = cargarPedidos() && estabaActivo;

        } catch (SQLIntegrityConstraintViolationException e) {
            mostrarAdvertencia(
                    "No se puede eliminar este pedido "
                            + "porque tiene entregas asociadas."
            );

        } catch (SQLException e) {
            mostrarErrorSQL("eliminar el pedido", e);

        } finally {
            if (estabaActivo && isDisplayable()) {
                timerActualizacion.start();
            }
        }
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

    private void mostrarErrorSQL(
            String operacion,
            SQLException error
    ) {
        System.err.println("Error al " + operacion + ".");
        error.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                "No fue posible " + operacion + ".\n"
                        + "Compruebe la conexión y disponibilidad de MySQL.",
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void detenerActualizacion() {
        if (timerActualizacion != null) {
            timerActualizacion.stop();
        }
    }

    @Override
    public void dispose() {
        detenerActualizacion();
        super.dispose();
    }
}