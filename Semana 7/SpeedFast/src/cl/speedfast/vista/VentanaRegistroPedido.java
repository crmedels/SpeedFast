package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.PedidoComida;
import cl.speedfast.modelo.PedidoEncomienda;
import cl.speedfast.modelo.PedidoExpress;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaRegistroPedido extends JFrame {

    private final ControladorDeEnvios controlador;

    private JTextField txtId;
    private JTextField txtDireccion;
    private JTextField txtDistancia;
    private JComboBox<String> cmbTipo;
    private JButton btnGuardar;
    private JButton btnVolver;

    public VentanaRegistroPedido(ControladorDeEnvios controlador) {
        this.controlador = controlador;

        configurarVentana();
        crearComponentes();
    }

    private void configurarVentana() {
        setTitle("SpeedFast - Registrar Pedido");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 20));
        panelPrincipal.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo =
                new JLabel("REGISTRO DE PEDIDO", SwingConstants.CENTER);

        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 15));

        JLabel lblId = new JLabel("ID:");
        JLabel lblDireccion = new JLabel("Dirección:");
        JLabel lblTipo = new JLabel("Tipo:");
        JLabel lblDistancia = new JLabel("Distancia (km):");

        txtId = new JTextField();
        txtDireccion = new JTextField();
        txtDistancia = new JTextField();

        cmbTipo = new JComboBox<>(new String[]{
                "Comida",
                "Encomienda",
                "Express"
        });

        panelFormulario.add(lblId);
        panelFormulario.add(txtId);

        panelFormulario.add(lblDireccion);
        panelFormulario.add(txtDireccion);

        panelFormulario.add(lblTipo);
        panelFormulario.add(cmbTipo);

        panelFormulario.add(lblDistancia);
        panelFormulario.add(txtDistancia);

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 0));

        btnGuardar = new JButton("Guardar");
        btnVolver = new JButton("Volver");

        panelBotones.add(btnGuardar);
        panelBotones.add(btnVolver);

        btnGuardar.addActionListener(e -> guardarPedido());

        btnVolver.addActionListener(e -> dispose());

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void guardarPedido() {

        String textoId = txtId.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String textoDistancia = txtDistancia.getText().trim();

        if (textoId.isEmpty()
                || direccion.isEmpty()
                || textoDistancia.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe completar todos los campos.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            int id = Integer.parseInt(textoId);
            int distancia = Integer.parseInt(textoDistancia);

            if (id <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "El ID debe ser mayor que cero.",
                        "ID inválido",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (distancia <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "La distancia debe ser mayor que cero.",
                        "Distancia inválida",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (controlador.existePedido(id)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ya existe un pedido con el ID " + id + ".",
                        "ID duplicado",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            String tipo = (String) cmbTipo.getSelectedItem();

            Pedido pedido;

            switch (tipo) {

                case "Comida":
                    pedido = new PedidoComida(
                            id,
                            direccion,
                            distancia
                    );
                    break;

                case "Encomienda":
                    pedido = new PedidoEncomienda(
                            id,
                            direccion,
                            distancia
                    );
                    break;

                case "Express":
                    pedido = new PedidoExpress(
                            id,
                            direccion,
                            distancia
                    );
                    break;

                default:
                    throw new IllegalStateException(
                            "Tipo de pedido no válido."
                    );
            }

            controlador.registrarPedido(pedido);

            JOptionPane.showMessageDialog(
                    this,
                    "Pedido registrado correctamente.",
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiarCampos();

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "El ID y la distancia deben contener solo números enteros.",
                    "Formato incorrecto",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtDireccion.setText("");
        txtDistancia.setText("");
        cmbTipo.setSelectedIndex(0);

        txtId.requestFocus();
    }
}