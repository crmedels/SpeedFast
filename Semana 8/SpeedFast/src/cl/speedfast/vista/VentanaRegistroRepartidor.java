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
 * Permite registrar, consultar, editar y eliminar repartidores.
 */
public class VentanaRegistroRepartidor extends JFrame {

    private final RepartidorDAO repartidorDAO;

    private JTextField txtNombre;
    private JTable tablaRepartidores;
    private DefaultTableModel modeloTabla;

    private JButton btnRegistrar;
    private JButton btnEditar;
    private JButton btnEliminar;

    private int idSeleccionado;

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
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(700, 400));
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
        tablaRepartidores.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tablaRepartidores.setRowHeight(25);
        tablaRepartidores.getTableHeader().setReorderingAllowed(false);

        tablaRepartidores.getColumnModel()
                .getColumn(0).setMaxWidth(80);

        tablaRepartidores.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        cargarSeleccion();
                    }
                });

        JPanel panelBotones =
                new JPanel(new GridLayout(1, 6, 8, 0));

        btnRegistrar = new JButton("Registrar");
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
        limpiarFormulario();
    }

    /**
     * Consulta MySQL y actualiza la tabla.
     */
    private void cargarRepartidores() {

        try {

            List<Repartidor> repartidores =
                    repartidorDAO.readAll();

            limpiarFormulario();
            modeloTabla.setRowCount(0);

            for (Repartidor repartidor : repartidores) {

                modeloTabla.addRow(
                        new Object[]{
                                repartidor.getIdRepartidor(),
                                repartidor.getNombre()
                        }
                );
            }

        } catch (SQLException e) {
            mostrarErrorSQL("cargar los repartidores", e);
        }
    }

    private void cargarSeleccion() {

        int fila = tablaRepartidores.getSelectedRow();

        if (fila < 0) {
            limpiarFormulario();
            return;
        }

        idSeleccionado =
                (Integer) modeloTabla.getValueAt(fila, 0);

        txtNombre.setText(
                (String) modeloTabla.getValueAt(fila, 1)
        );

        btnRegistrar.setEnabled(false);
        btnEditar.setEnabled(true);
        btnEliminar.setEnabled(true);
    }

    private boolean validarNombre() {

        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {

            mostrarAdvertencia(
                    "Debe ingresar el nombre del repartidor."
            );

            txtNombre.requestFocusInWindow();
            return false;
        }

        if (nombre.length() > 100) {

            mostrarAdvertencia(
                    "El nombre no puede superar los 100 caracteres."
            );

            txtNombre.requestFocusInWindow();
            return false;
        }

        return true;
    }

    private boolean validarSeleccion() {

        if (idSeleccionado <= 0) {

            mostrarAdvertencia(
                    "Seleccione un repartidor de la tabla."
            );
            return false;
        }

        return true;
    }

    private void registrarRepartidor() {

        if (!validarNombre()) {
            return;
        }

        Repartidor repartidor = new Repartidor(
                0,
                txtNombre.getText().trim()
        );

        try {

            if (repartidorDAO.create(repartidor)) {

                mostrarExito(
                        "Repartidor registrado correctamente.\n"
                                + "ID generado: "
                                + repartidor.getIdRepartidor()
                );

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

    private void editarRepartidor() {

        if (!validarSeleccion() || !validarNombre()) {
            return;
        }

        Repartidor repartidor = new Repartidor(
                idSeleccionado,
                txtNombre.getText().trim()
        );

        try {

            if (repartidorDAO.update(repartidor)) {

                mostrarExito(
                        "Repartidor actualizado correctamente."
                );

                cargarRepartidores();

            } else {

                mostrarAdvertencia(
                        "El repartidor ya no existe en la base de datos."
                );

                cargarRepartidores();
            }

        } catch (SQLException e) {
            mostrarErrorSQL("editar el repartidor", e);
        }
    }

    private void eliminarRepartidor() {

        if (!validarSeleccion()) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Desea eliminar el repartidor #"
                        + idSeleccionado
                        + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (repartidorDAO.delete(idSeleccionado)) {

                mostrarExito(
                        "Repartidor eliminado correctamente."
                );

                cargarRepartidores();

            } else {

                mostrarAdvertencia(
                        "El repartidor ya no existe en la base de datos."
                );

                cargarRepartidores();
            }

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

        idSeleccionado = 0;

        tablaRepartidores.clearSelection();
        txtNombre.setText("");

        btnRegistrar.setEnabled(true);
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);

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