package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.PedidoComida;
import cl.speedfast.modelo.PedidoEncomienda;
import cl.speedfast.modelo.PedidoExpress;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaListaPedidos extends JFrame {

    private final ControladorDeEnvios controlador;

    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private JButton btnRefrescar;
    private JButton btnVolver;

    public VentanaListaPedidos(ControladorDeEnvios controlador) {
        this.controlador = controlador;

        configurarVentana();
        crearComponentes();
        cargarPedidos();
    }

    private void configurarVentana() {
        setTitle("SpeedFast - Lista de Pedidos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 450);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 15));
        panelPrincipal.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo =
                new JLabel("LISTADO DE PEDIDOS", SwingConstants.CENTER);

        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        String[] columnas = {
                "ID",
                "Tipo",
                "Dirección",
                "Distancia",
                "Tiempo estimado",
                "Repartidor",
                "Estado"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaPedidos = new JTable(modeloTabla);
        tablaPedidos.setRowHeight(25);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollTabla = new JScrollPane(tablaPedidos);

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 0));

        btnRefrescar = new JButton("Refrescar");
        btnVolver = new JButton("Volver");

        panelBotones.add(btnRefrescar);
        panelBotones.add(btnVolver);

        btnRefrescar.addActionListener(e -> cargarPedidos());

        btnVolver.addActionListener(e -> dispose());

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(scrollTabla, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void cargarPedidos() {

        modeloTabla.setRowCount(0);

        for (Pedido pedido : controlador.getPedidosRegistrados()) {

            String repartidor = pedido.getNombreRepartidor();

            if (repartidor == null) {
                repartidor = "Sin asignar";
            }

            Object[] fila = {
                    pedido.getIdPedido(),
                    obtenerTipoPedido(pedido),
                    pedido.getDireccionEntrega(),
                    pedido.getDistanciaKm() + " km",
                    pedido.calcularTiempoEntrega() + " min",
                    repartidor,
                    pedido.getEstado()
            };

            modeloTabla.addRow(fila);
        }
    }

    private String obtenerTipoPedido(Pedido pedido) {

        if (pedido instanceof PedidoComida) {
            return "Comida";
        }

        if (pedido instanceof PedidoEncomienda) {
            return "Encomienda";
        }

        if (pedido instanceof PedidoExpress) {
            return "Express";
        }

        return "Desconocido";
    }
}