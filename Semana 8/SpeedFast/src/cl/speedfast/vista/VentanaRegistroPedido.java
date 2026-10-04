package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.PedidoComida;
import cl.speedfast.modelo.PedidoEncomienda;
import cl.speedfast.modelo.PedidoExpress;
import dao.PedidoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * Registra pedidos pendientes y valida los datos antes de guardarlos.
 */
public class VentanaRegistroPedido extends JFrame {

    private final ControladorDeEnvios controlador;
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    private JTextField txtDireccion;
    private JTextField txtDistancia;
    private JComboBox<String> cmbTipo;

    public VentanaRegistroPedido(ControladorDeEnvios controlador) {
        this.controlador = controlador;
        configurarVentana();
        crearComponentes();
    }

    private void configurarVentana() {
        setTitle("SpeedFast - Registrar Pedido");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 320);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {
        JPanel principal = new JPanel(new BorderLayout(10, 20));
        principal.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titulo = new JLabel(
                "REGISTRO DE PEDIDO", SwingConstants.CENTER
        );
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        txtDireccion = new JTextField();
        txtDistancia = new JTextField();
        cmbTipo = new JComboBox<>(
                new String[]{"Comida", "Encomienda", "Express"}
        );

        JPanel formulario = new JPanel(new GridLayout(3, 2, 10, 15));
        formulario.add(new JLabel("Dirección:"));
        formulario.add(txtDireccion);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(cmbTipo);
        formulario.add(new JLabel("Distancia (km):"));
        formulario.add(txtDistancia);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnVolver = new JButton("Volver");

        JPanel botones = new JPanel(new GridLayout(1, 2, 10, 0));
        botones.add(btnGuardar);
        botones.add(btnVolver);

        btnGuardar.addActionListener(e -> guardarPedido());
        btnVolver.addActionListener(e -> dispose());

        principal.add(titulo, BorderLayout.NORTH);
        principal.add(formulario, BorderLayout.CENTER);
        principal.add(botones, BorderLayout.SOUTH);
        add(principal);
    }

    private void guardarPedido() {
        String direccion = txtDireccion.getText().trim();
        String textoDistancia = txtDistancia.getText().trim();
        String tipo = (String) cmbTipo.getSelectedItem();

        if (direccion.isEmpty() || textoDistancia.isEmpty()) {
            mostrarAdvertencia("Debe completar todos los campos.");
            return;
        }

        if (direccion.length() > 150) {
            mostrarAdvertencia(
                    "La dirección no puede superar los 150 caracteres."
            );
            txtDireccion.requestFocusInWindow();
            return;
        }

        if (tipo == null) {
            mostrarAdvertencia("Debe seleccionar un tipo de pedido.");
            return;
        }

        int distancia;

        try {
            distancia = Integer.parseInt(textoDistancia);

        } catch (NumberFormatException e) {
            mostrarAdvertencia(
                    "La distancia debe ser un número entero válido."
            );
            txtDistancia.requestFocusInWindow();
            return;
        }

        if (distancia <= 0) {
            mostrarAdvertencia("La distancia debe ser mayor que cero.");
            txtDistancia.requestFocusInWindow();
            return;
        }

        try {
            Pedido pedido = switch (tipo) {
                case "Comida" ->
                        new PedidoComida(0, direccion, distancia);

                case "Encomienda" ->
                        new PedidoEncomienda(0, direccion, distancia);

                case "Express" ->
                        new PedidoExpress(0, direccion, distancia);

                default -> throw new IllegalArgumentException(
                        "El tipo de pedido no es válido."
                );
            };

            if (!pedidoDAO.create(pedido)) {
                mostrarAdvertencia("No se pudo registrar el pedido.");
                return;
            }

            controlador.registrarPedido(pedido);

            JOptionPane.showMessageDialog(
                    this,
                    "Pedido registrado correctamente.\n"
                            + "ID generado: " + pedido.getIdPedido()
                            + "\nEstado inicial: " + pedido.getEstado(),
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiarCampos();

        } catch (SQLException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible registrar el pedido.\n"
                            + "Compruebe la conexión y disponibilidad de MySQL.",
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException e) {
            mostrarAdvertencia(e.getMessage());
        }
    }

    private void limpiarCampos() {
        txtDireccion.setText("");
        txtDistancia.setText("");
        cmbTipo.setSelectedIndex(0);
        txtDireccion.requestFocusInWindow();
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Datos inválidos",
                JOptionPane.WARNING_MESSAGE
        );
    }
}