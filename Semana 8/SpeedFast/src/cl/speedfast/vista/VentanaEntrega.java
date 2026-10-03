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
import java.awt.*;
import java.time.LocalDateTime;

public class VentanaEntrega extends JFrame {

    private final ControladorDeEnvios controlador;
    private final RepartidorDAO repartidorDAO;
    private final PedidoDAO pedidoDAO;
    private final EntregaDAO entregaDAO;

    private Repartidor repartidorAsignado;
    private Pedido pedidoConRepartidorAsignado;

    private JComboBox<Pedido> cmbPedidos;
    private JComboBox<Repartidor> cmbRepartidores;

    public VentanaEntrega(ControladorDeEnvios controlador) {

        this.controlador = controlador;
        this.repartidorDAO = new RepartidorDAO();
        this.pedidoDAO = new PedidoDAO();
        this.entregaDAO = new EntregaDAO();

        configurarVentana();
        crearComponentes();
        refrescarDatos();
    }

    private void configurarVentana() {

        setTitle("SpeedFast - Gestión de Entregas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 350);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {

        JPanel panelPrincipal =
                new JPanel(new BorderLayout(10, 20));

        panelPrincipal.setBorder(
                new EmptyBorder(20, 30, 20, 30)
        );

        JLabel lblTitulo = new JLabel(
                "GESTIÓN DE ENTREGAS",
                SwingConstants.CENTER
        );

        lblTitulo.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        JPanel panelFormulario =
                new JPanel(new GridLayout(2, 2, 10, 15));

        JLabel lblPedido = new JLabel("Pedido:");
        JLabel lblRepartidor = new JLabel("Repartidor:");

        cmbPedidos = new JComboBox<>();
        cmbRepartidores = new JComboBox<>();

        panelFormulario.add(lblPedido);
        panelFormulario.add(cmbPedidos);
        panelFormulario.add(lblRepartidor);
        panelFormulario.add(cmbRepartidores);

        JPanel panelBotones =
                new JPanel(new GridLayout(2, 2, 10, 10));

        JButton btnAsignar =
                new JButton("Asignar repartidor");

        JButton btnIniciarEntrega =
                new JButton("Iniciar entrega");

        JButton btnRefrescar =
                new JButton("Refrescar");

        JButton btnVolver =
                new JButton("Volver");

        panelBotones.add(btnAsignar);
        panelBotones.add(btnIniciarEntrega);
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnVolver);

        btnAsignar.addActionListener(
                e -> asignarRepartidor()
        );

        btnIniciarEntrega.addActionListener(
                e -> iniciarEntrega()
        );

        btnRefrescar.addActionListener(
                e -> refrescarDatos()
        );

        btnVolver.addActionListener(
                e -> dispose()
        );

        // Cambiar la selección requiere una nueva asignación.
        cmbPedidos.addActionListener(
                e -> limpiarAsignacion()
        );

        cmbRepartidores.addActionListener(
                e -> limpiarAsignacion()
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

    private void cargarPedidos() {

        cmbPedidos.removeAllItems();

        for (Pedido pedido : pedidoDAO.listarPedidos()) {
            cmbPedidos.addItem(pedido);
        }
    }

    private void cargarRepartidores() {

        cmbRepartidores.removeAllItems();

        for (Repartidor repartidor : repartidorDAO.listarTodos()) {
            cmbRepartidores.addItem(repartidor);
        }
    }

    private void refrescarDatos() {

        limpiarAsignacion();
        cargarPedidos();
        cargarRepartidores();
    }

    private void limpiarAsignacion() {

        repartidorAsignado = null;
        pedidoConRepartidorAsignado = null;
    }

    private void mostrarAdvertencia(String mensaje) {

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Operación no disponible",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private boolean validarPedidoPendiente(Pedido pedido) {

        if (pedido == null) {
            mostrarAdvertencia(
                    "No existen pedidos disponibles para seleccionar."
            );
            return false;
        }

        if (pedido.getEstado() == EstadoPedido.EN_REPARTO) {
            mostrarAdvertencia(
                    "El pedido ya se encuentra en reparto."
            );
            return false;
        }

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            mostrarAdvertencia(
                    "El pedido ya fue entregado."
            );
            return false;
        }

        return true;
    }

    private void asignarRepartidor() {

        Pedido pedido =
                (Pedido) cmbPedidos.getSelectedItem();

        Repartidor repartidor =
                (Repartidor) cmbRepartidores.getSelectedItem();

        if (!validarPedidoPendiente(pedido)) {
            return;
        }

        if (repartidor == null) {
            mostrarAdvertencia(
                    "No existen repartidores disponibles para seleccionar."
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
                        + " asignado al pedido #"
                        + pedido.getIdPedido()
                        + ".\nPresione Iniciar entrega para guardar.",
                "Asignación realizada",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void iniciarEntrega() {

        Pedido pedido =
                (Pedido) cmbPedidos.getSelectedItem();

        if (!validarPedidoPendiente(pedido)) {
            return;
        }

        if (repartidorAsignado == null
                || pedidoConRepartidorAsignado != pedido) {

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
                ahora.toLocalTime()
        );

        if (!entregaDAO.guardar(entrega)) {

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
                    "La entrega fue registrada, pero no fue posible "
                            + "actualizar el estado del pedido.",
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        controlador.reservarPedido(pedido);
        controlador.despacharPedido(pedido);
        pedido.setEstado(EstadoPedido.EN_REPARTO);

        JOptionPane.showMessageDialog(
                this,
                "La entrega del pedido #"
                        + pedido.getIdPedido()
                        + " ha comenzado.\n"
                        + "Entrega registrada con ID "
                        + entrega.getIdEntrega()
                        + ".",
                "Entrega iniciada",
                JOptionPane.INFORMATION_MESSAGE
        );

        refrescarDatos();
    }
}