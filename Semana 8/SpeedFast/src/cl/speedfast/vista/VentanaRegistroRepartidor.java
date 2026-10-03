package cl.speedfast.vista;

import cl.speedfast.modelo.Repartidor;
import dao.RepartidorDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Gestiona repartidores y permite editarlos mediante un diálogo.
 */
public class VentanaRegistroRepartidor extends JFrame {

    private final RepartidorDAO repartidorDAO;

    private JTextField txtNombre;
    private JTable tablaRepartidores;
    private DefaultTableModel modeloTabla;

    private JButton btnEditar;
    private JButton btnEliminar;

    public VentanaRegistroRepartidor() {

        repartidorDAO = new RepartidorDAO();

        configurarVentana();
        crearComponentes();
        cargarRepartidores();
    }

    private void configurarVentana() {

        setTitle("SpeedFast - Gestión de Repartidores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 450);
        setMinimumSize(new Dimension(700, 400));
        setLocationRelativeTo(null);
    }

    private void crearComponentes() {

        JPanel panelPrincipal =
                new JPanel(new BorderLayout(10, 15));

        panelPrincipal.setBorder(
                new EmptyBorder(20, 25, 20, 25)
        );

        JLabel lblTitulo = new JLabel(
                "GESTIÓN DE REPARTIDORES",
                SwingConstants.CENTER
        );

        lblTitulo.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        JPanel panelFormulario =
                new JPanel(new BorderLayout(10, 0));

        panelFormulario.setBorder(
                BorderFactory.createTitledBorder(
                        "Registrar nuevo repartidor"
                )
        );

        txtNombre = new JTextField();

        panelFormulario.add(
                new JLabel("Nombre:"),
                BorderLayout.WEST
        );

        panelFormulario.add(
                txtNombre,
                BorderLayout.CENTER
        );

        JPanel panelSuperior =
                new JPanel(new BorderLayout(10, 15));

        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(panelFormulario, BorderLayout.CENTER);

        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Nombre"},
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaRepartidores = new JTable(modeloTabla);
        tablaRepartidores.setRowHeight(25);
        tablaRepartidores.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tablaRepartidores.getTableHeader()
                .setReorderingAllowed(false);

        tablaRepartidores.getColumnModel()
                .getColumn(0).setMaxWidth(80);

        tablaRepartidores.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        actualizarBotones();
                    }
                });

        JPanel panelBotones =
                new JPanel(new GridLayout(1, 6, 8, 0));

        JButton btnRegistrar = new JButton("Registrar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");

        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnRefrescar = new JButton("Refrescar");
        JButton btnVolver = new JButton("Volver");

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnVolver);

        btnRegistrar.addActionListener(e -> registrarRepartidor());
        btnEditar.addActionListener(e -> editarRepartidor());
        btnEliminar.addActionListener(e -> eliminarRepartidor());

        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnRefrescar.addActionListener(e -> cargarRepartidores());
        btnVolver.addActionListener(e -> dispose());

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        panelPrincipal.add(
                new JScrollPane(tablaRepartidores),
                BorderLayout.CENTER
        );

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
        actualizarBotones();
    }

    private void cargarRepartidores() {

        try {

            List<Repartidor> repartidores =
                    repartidorDAO.readAll();

            modeloTabla.setRowCount(0);

            for (Repartidor repartidor : repartidores) {

                modeloTabla.addRow(
                        new Object[]{
                                repartidor.getIdRepartidor(),
                                repartidor.getNombre()
                        }
                );
            }

            actualizarBotones();

        } catch (SQLException e) {
            mostrarErrorSQL("cargar los repartidores", e);
        }
    }

    private void actualizarBotones() {

        boolean haySeleccion =
                tablaRepartidores.getSelectedRow() >= 0;

        btnEditar.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion);
    }

    private int obtenerFilaSeleccionada() {

        int fila = tablaRepartidores.getSelectedRow();

        if (fila < 0) {
            mostrarAdvertencia(
                    "Seleccione un repartidor de la tabla."
            );
        }

        return fila;
    }

    private boolean validarNombre(String nombre) {

        if (nombre.isEmpty()) {

            mostrarAdvertencia(
                    "Debe ingresar el nombre del repartidor."
            );
            return false;
        }

        if (nombre.length() > 100) {

            mostrarAdvertencia(
                    "El nombre no puede superar los 100 caracteres."
            );
            return false;
        }

        return true;
    }

    private void registrarRepartidor() {

        String nombre = txtNombre.getText().trim();

        if (!validarNombre(nombre)) {
            txtNombre.requestFocusInWindow();
            return;
        }

        Repartidor repartidor =
                new Repartidor(0, nombre);

        try {

            if (repartidorDAO.create(repartidor)) {

                mostrarExito(
                        "Repartidor registrado correctamente.\n"
                                + "ID generado: "
                                + repartidor.getIdRepartidor()
                );

                limpiarFormulario();
                cargarRepartidores();

            } else {
                mostrarAdvertencia(
                        "No se pudo registrar el repartidor."
                );
            }

        } catch (SQLException e) {
            mostrarErrorSQL("registrar el repartidor", e);
        }
    }

    /**
     * Abre un diálogo con el nombre del repartidor seleccionado.
     */
    private void editarRepartidor() {

        int fila = obtenerFilaSeleccionada();

        if (fila < 0) {
            return;
        }

        int idRepartidor =
                (Integer) modeloTabla.getValueAt(fila, 0);

        JTextField txtNombreEditar = new JTextField(
                (String) modeloTabla.getValueAt(fila, 1),
                25
        );

        JPanel formulario =
                new JPanel(new BorderLayout(10, 0));

        formulario.add(
                new JLabel("Nombre:"),
                BorderLayout.WEST
        );

        formulario.add(
                txtNombreEditar,
                BorderLayout.CENTER
        );

        while (true) {

            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    formulario,
                    "Editar repartidor #" + idRepartidor,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (respuesta != JOptionPane.OK_OPTION) {
                return;
            }

            String nombre = txtNombreEditar.getText().trim();

            if (!validarNombre(nombre)) {
                continue;
            }

            Repartidor repartidor =
                    new Repartidor(idRepartidor, nombre);

            try {

                if (repartidorDAO.update(repartidor)) {

                    mostrarExito(
                            "Repartidor actualizado correctamente."
                    );

                } else {

                    mostrarAdvertencia(
                            "El repartidor ya no existe en la base de datos."
                    );
                }

                cargarRepartidores();

            } catch (SQLException e) {
                mostrarErrorSQL("editar el repartidor", e);
            }

            return;
        }
    }

    private void eliminarRepartidor() {

        int fila = obtenerFilaSeleccionada();

        if (fila < 0) {
            return;
        }

        int idRepartidor =
                (Integer) modeloTabla.getValueAt(fila, 0);

        String nombre =
                (String) modeloTabla.getValueAt(fila, 1);

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea eliminar el repartidor #"
                        + idRepartidor
                        + " - "
                        + nombre
                        + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (repartidorDAO.delete(idRepartidor)) {

                mostrarExito(
                        "Repartidor eliminado correctamente."
                );

            } else {

                mostrarAdvertencia(
                        "El repartidor ya no existe en la base de datos."
                );
            }

            cargarRepartidores();

        } catch (SQLIntegrityConstraintViolationException e) {

            mostrarAdvertencia(
                    "No se puede eliminar este repartidor "
                            + "porque tiene entregas asociadas."
            );

        } catch (SQLException e) {
            mostrarErrorSQL("eliminar el repartidor", e);
        }
    }

    private void limpiarFormulario() {

        txtNombre.setText("");
        tablaRepartidores.clearSelection();
        actualizarBotones();
        txtNombre.requestFocusInWindow();
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
}