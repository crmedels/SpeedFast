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

public class VentanaRegistroPedido extends JFrame {

    private final ControladorDeEnvios controlador;
    private final PedidoDAO pedidoDAO;

    private JTextField txtDireccion;
    private JTextField txtDistancia;
    private JComboBox<String> cmbTipo;
    private JButton btnGuardar;
    private JButton btnVolver;

    public VentanaRegistroPedido(ControladorDeEnvios controlador) {

        this.controlador = controlador;
        this.pedidoDAO = new PedidoDAO();

        configurarVentana();
        crearComponentes();
    }

    private void configurarVentana() {

        setTitle("SpeedFast - Registrar Pedido");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 320);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {

        JPanel panelPrincipal =
                new JPanel(new BorderLayout(10, 20));

        panelPrincipal.setBorder(
                new EmptyBorder(20, 30, 20, 30)
        );

        JLabel lblTitulo =
                new JLabel(
                        "REGISTRO DE PEDIDO",
                        SwingConstants.CENTER
                );

        lblTitulo.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        JPanel panelFormulario =
                new JPanel(new GridLayout(3, 2, 10, 15));

        JLabel lblDireccion =
                new JLabel("Dirección:");

        JLabel lblTipo =
                new JLabel("Tipo:");

        JLabel lblDistancia =
                new JLabel("Distancia (km):");

        txtDireccion = new JTextField();
        txtDistancia = new JTextField();

        cmbTipo = new JComboBox<>(
                new String[]{
                        "Comida",
                        "Encomienda",
                        "Express"
                }
        );

        panelFormulario.add(lblDireccion);
        panelFormulario.add(txtDireccion);

        panelFormulario.add(lblTipo);
        panelFormulario.add(cmbTipo);

        panelFormulario.add(lblDistancia);
        panelFormulario.add(txtDistancia);

        JPanel panelBotones =
                new JPanel(new GridLayout(1, 2, 10, 0));

        btnGuardar = new JButton("Guardar");
        btnVolver = new JButton("Volver");

        panelBotones.add(btnGuardar);
        panelBotones.add(btnVolver);

        btnGuardar.addActionListener(
                e -> guardarPedido()
        );

        btnVolver.addActionListener(
                e -> dispose()
        );

        panelPrincipal.add(
                lblTitulo,
                BorderLayout.NORTH
        );

        panelPrincipal.add(
                panelFormulario,
                BorderLayout.CENTER
        );

        panelPrincipal.add(
                panelBotones,
                BorderLayout.SOUTH
        );

        add(panelPrincipal);
    }

    private void guardarPedido() {

        String direccion =
                txtDireccion.getText().trim();

        String textoDistancia =
                txtDistancia.getText().trim();

        if (direccion.isEmpty()
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

            int distancia =
                    Integer.parseInt(textoDistancia);

            if (distancia <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "La distancia debe ser mayor que cero.",
                        "Distancia inválida",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            String tipo =
                    (String) cmbTipo.getSelectedItem();

            Pedido pedido;

            switch (tipo) {

                case "Comida":
                    pedido = new PedidoComida(
                            0,
                            direccion,
                            distancia
                    );
                    break;

                case "Encomienda":
                    pedido = new PedidoEncomienda(
                            0,
                            direccion,
                            distancia
                    );
                    break;

                case "Express":
                    pedido = new PedidoExpress(
                            0,
                            direccion,
                            distancia
                    );
                    break;

                default:
                    throw new IllegalStateException(
                            "Tipo de pedido no válido."
                    );
            }

            if (pedidoDAO.guardar(pedido)) {

                controlador.registrarPedido(pedido);

                JOptionPane.showMessageDialog(
                        this,
                        "Pedido registrado correctamente.\n"
                                + "ID generado: "
                                + pedido.getIdPedido(),
                        "Registro exitoso",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarCampos();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo guardar el pedido "
                                + "en la base de datos.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "La distancia debe contener "
                            + "solo números enteros.",
                    "Formato incorrecto",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarCampos() {

        txtDireccion.setText("");
        txtDistancia.setText("");
        cmbTipo.setSelectedIndex(0);

        txtDireccion.requestFocus();
    }
}