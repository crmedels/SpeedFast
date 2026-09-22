package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private final ControladorDeEnvios controlador;

    private JButton btnRegistrarPedido;
    private JButton btnListarPedidos;
    private JButton btnGestionarEntrega;
    private JButton btnSalir;

    public VentanaPrincipal(ControladorDeEnvios controlador) {
        this.controlador = controlador;

        configurarVentana();
        crearComponentes();
    }

    private void configurarVentana() {
        setTitle("SpeedFast - Sistema de Entregas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 20));
        panelPrincipal.setBorder(new EmptyBorder(25, 40, 25, 40));

        JLabel lblTitulo = new JLabel("SISTEMA DE ENTREGAS SPEEDFAST", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel panelBotones = new JPanel(new GridLayout(4, 1, 10, 10));

        btnRegistrarPedido = new JButton("Registrar pedido");
        btnListarPedidos = new JButton("Listar pedidos");
        btnGestionarEntrega = new JButton("Asignar repartidor / Iniciar entrega");
        btnSalir = new JButton("Salir");

        panelBotones.add(btnRegistrarPedido);
        panelBotones.add(btnListarPedidos);
        panelBotones.add(btnGestionarEntrega);
        panelBotones.add(btnSalir);

        btnSalir.addActionListener(e -> System.exit(0));

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(panelBotones, BorderLayout.CENTER);

        add(panelPrincipal);
    }
}
