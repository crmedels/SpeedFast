package cl.speedfast.main;

import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.PedidoComida;
import dao.PedidoDAO;

public class PruebaPedidoDAO {

    public static void main(String[] args) {

        Pedido pedido = new PedidoComida(
                0,
                "Los Carrera 123",
                5
        );

        PedidoDAO pedidoDAO = new PedidoDAO();

        if (pedidoDAO.guardar(pedido)) {
            System.out.println("Pedido guardado correctamente.");
            System.out.println("ID generado: " + pedido.getIdPedido());
        } else {
            System.out.println("No se pudo guardar el pedido.");
        }
    }
}