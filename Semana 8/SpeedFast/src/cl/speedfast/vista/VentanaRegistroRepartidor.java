package cl.speedfast.vista;

import cl.speedfast.modelo.Repartidor;
import dao.RepartidorDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaRegistroRepartidor extends JFrame {

    private final RepartidorDAO repartidorDAO;

    private JTextField txtNombre;
    private JButton btnGuardar;
    private JButton btnVolver;

    public VentanaRegistroRepartidor() {

        this.repartidorDAO = new RepartidorDAO();

        configurarVentana();
        crearComponentes();
    }

    private void configurarVentana() {

        setTitle("SpeedFast - Registrar Repartidor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 230);
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
                        "REGISTRO DE REPARTIDOR",
                        SwingConstants.CENTER
                );

        lblTitulo.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        JPanel panelFormulario =
                new JPanel(new GridLayout(1, 2, 10, 10));

        JLabel lblNombre =
                new JLabel("Nombre:");

        txtNombre = new JTextField();

        panelFormulario.add(lblNombre);
        panelFormulario.add(txtNombre);

        JPanel panelBotones =
                new JPanel(new GridLayout(1, 2, 10, 0));

        btnGuardar = new JButton("Guardar");
        btnVolver = new JButton("Volver");

        panelBotones.add(btnGuardar);
        panelBotones.add(btnVolver);

        btnGuardar.addActionListener(
                e -> guardarRepartidor()
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

    private void guardarRepartidor() {

        String nombre =
                txtNombre.getText().trim();

        if (nombre.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe ingresar el nombre del repartidor.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Repartidor repartidor =
                new Repartidor(0, nombre);

        if (repartidorDAO.guardar(repartidor)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Repartidor registrado correctamente.\n"
                            + "ID generado: "
                            + repartidor.getIdRepartidor(),
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE
            );

            txtNombre.setText("");
            txtNombre.requestFocus();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo guardar el repartidor "
                            + "en la base de datos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}