package cl.speedfast.vista;

import cl.speedfast.controlador.ControladorDeEnvios;
import dao.PedidoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VentanaListaPedidos extends JFrame {

    private final PedidoDAO pedidoDAO;

    private JTable tablaPedidos;
    private DefaultTableModel modeloTabla;
    private JButton btnRefrescar;
    private JButton btnVolver;

    public VentanaListaPedidos(ControladorDeEnvios controlador) {

        this.pedidoDAO = new PedidoDAO();

        configurarVentana();
        crearComponentes();
        cargarPedidos();
    }

    private void configurarVentana() {

        setTitle("SpeedFast - Lista de Pedidos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearComponentes() {

        JPanel panelPrincipal =
                new JPanel(new BorderLayout(10, 15));

        panelPrincipal.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JLabel lblTitulo =
                new JLabel(
                        "PEDIDOS REGISTRADOS EN BASE DE DATOS",
                        SwingConstants.CENTER
                );

        lblTitulo.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        String[] columnas = {
                "ID",
                "Tipo",
                "Dirección",
                "Estado"
        };

        modeloTabla =
                new DefaultTableModel(columnas, 0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };

        tablaPedidos = new JTable(modeloTabla);
        tablaPedidos.setRowHeight(25);

        tablaPedidos.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollTabla =
                new JScrollPane(tablaPedidos);

        JPanel panelBotones =
                new JPanel(new GridLayout(1, 2, 10, 0));

        btnRefrescar = new JButton("Refrescar");
        btnVolver = new JButton("Volver");

        panelBotones.add(btnRefrescar);
        panelBotones.add(btnVolver);

        btnRefrescar.addActionListener(
                e -> cargarPedidos()
        );

        btnVolver.addActionListener(
                e -> dispose()
        );

        panelPrincipal.add(
                lblTitulo,
                BorderLayout.NORTH
        );

        panelPrincipal.add(
                scrollTabla,
                BorderLayout.CENTER
        );

        panelPrincipal.add(
                panelBotones,
                BorderLayout.SOUTH
        );

        add(panelPrincipal);
    }

    private void cargarPedidos() {

        modeloTabla.setRowCount(0);

        for (Object[] pedido : pedidoDAO.listarTodos()) {
            modeloTabla.addRow(pedido);
        }
    }
}