package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;
import cl.speedfast.modelo.EstadoPedido;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.Repartidor;
import cl.speedfast.modelo.Entrega;
import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class VentanaEntrega extends JFrame {

    private final ControladorDeEnvios controlador;
    private final RepartidorDAO repartidorDAO;
    private final PedidoDAO pedidoDAO;
    private final EntregaDAO entregaDAO;

    private Repartidor repartidorAsignado;
    private Pedido pedidoConRepartidorAsignado;

    private JComboBox<Pedido> cmbPedidos;
    private JTextField txtRepartidor;

    private JButton btnAsignar;
    private JButton btnIniciarEntrega;
    private JButton btnRefrescar;
    private JButton btnVolver;

    public VentanaEntrega(ControladorDeEnvios controlador) {
        this.controlador = controlador;
        this.repartidorDAO = new RepartidorDAO();
        this.pedidoDAO = new PedidoDAO();
        this.entregaDAO = new EntregaDAO();

        configurarVentana();
        crearComponentes();
        cargarPedidos();
    }

    private void configurarVentana() {
        setTitle("SpeedFast - Gestión de Entregas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 350);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 20));
        panelPrincipal.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitulo =
                new JLabel("GESTIÓN DE ENTREGAS", SwingConstants.CENTER);

        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel panelFormulario = new JPanel(new GridLayout(2, 2, 10, 15));

        JLabel lblPedido = new JLabel("Pedido:");
        JLabel lblRepartidor = new JLabel("ID Repartidor:");

        cmbPedidos = new JComboBox<>();
        txtRepartidor = new JTextField();

        panelFormulario.add(lblPedido);
        panelFormulario.add(cmbPedidos);

        panelFormulario.add(lblRepartidor);
        panelFormulario.add(txtRepartidor);

        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 10, 10));

        btnAsignar = new JButton("Asignar repartidor");
        btnIniciarEntrega = new JButton("Iniciar entrega");
        btnRefrescar = new JButton("Refrescar");
        btnVolver = new JButton("Volver");

        panelBotones.add(btnAsignar);
        panelBotones.add(btnIniciarEntrega);
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnVolver);

        btnAsignar.addActionListener(e -> asignarRepartidor());
        btnIniciarEntrega.addActionListener(e -> iniciarEntrega());
        btnRefrescar.addActionListener(e -> cargarPedidos());
        btnVolver.addActionListener(e -> dispose());

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void cargarPedidos() {

        cmbPedidos.removeAllItems();

        for (Pedido pedido : controlador.getPedidosRegistrados()) {
            cmbPedidos.addItem(pedido);
        }
    }

    private void asignarRepartidor() {

        Pedido pedido = (Pedido) cmbPedidos.getSelectedItem();
        String textoIdRepartidor = txtRepartidor.getText().trim();

        if (pedido == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existen pedidos registrados.",
                    "Sin pedidos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (textoIdRepartidor.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe ingresar el ID del repartidor.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {

            JOptionPane.showMessageDialog(
                    this,
                    "El pedido ya fue entregado.",
                    "Operación no disponible",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int idRepartidor;

        try {

            idRepartidor = Integer.parseInt(textoIdRepartidor);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "El ID del repartidor debe ser un número entero.",
                    "ID inválido",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Repartidor repartidor =
                repartidorDAO.buscarPorId(idRepartidor);

        if (repartidor == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existe un repartidor con el ID ingresado.",
                    "Repartidor no encontrado",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        pedido.asignarRepartidor(
                repartidor.getNombre()
        );

        repartidorAsignado = repartidor;
        pedidoConRepartidorAsignado = pedido;

        JOptionPane.showMessageDialog(
                this,
                "Repartidor "
                        + repartidor.getNombre()
                        + " asignado correctamente al pedido #"
                        + pedido.getIdPedido()
                        + ".",
                "Asignación exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );

        txtRepartidor.setText("");
    }

    private void iniciarEntrega() {

        Pedido pedido = (Pedido) cmbPedidos.getSelectedItem();

        if (pedido == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "No existen pedidos registrados.",
                    "Sin pedidos",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (repartidorAsignado == null
                || pedidoConRepartidorAsignado != pedido) {

            JOptionPane.showMessageDialog(
                    this,
                    "Debe asignar un repartidor antes de iniciar la entrega.",
                    "Repartidor no asignado",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (pedido.getEstado() == EstadoPedido.EN_REPARTO) {

            JOptionPane.showMessageDialog(
                    this,
                    "El pedido ya se encuentra en reparto.",
                    "Entrega iniciada",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {

            JOptionPane.showMessageDialog(
                    this,
                    "El pedido ya fue entregado.",
                    "Operación no disponible",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Entrega entrega = new Entrega(
                pedido.getIdPedido(),
                repartidorAsignado.getIdRepartidor(),
                LocalDate.now(),
                LocalTime.now()
        );

        boolean entregaGuardada =
                entregaDAO.guardar(entrega);

        if (!entregaGuardada) {

            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible guardar la entrega en la base de datos.",
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        boolean estadoActualizado =
                pedidoDAO.actualizarEstado(
                        pedido.getIdPedido(),
                        EstadoPedido.EN_REPARTO
                );

        if (!estadoActualizado) {

            JOptionPane.showMessageDialog(
                    this,
                    "La entrega fue registrada, pero no fue posible actualizar el estado del pedido.",
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        controlador.reservarPedido(pedido);
        controlador.despacharPedido(pedido);

        pedido.setEstado(
                EstadoPedido.EN_REPARTO
        );

        JOptionPane.showMessageDialog(
                this,
                "La entrega del pedido #"
                        + pedido.getIdPedido()
                        + " ha comenzado."
                        + "\nEntrega registrada con ID "
                        + entrega.getIdEntrega()
                        + ".",
                "Entrega iniciada",
                JOptionPane.INFORMATION_MESSAGE
        );

        repartidorAsignado = null;
        pedidoConRepartidorAsignado = null;

        cargarPedidos();
    }
}