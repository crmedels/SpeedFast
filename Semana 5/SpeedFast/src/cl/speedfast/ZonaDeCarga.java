package cl.speedfast;

import java.util.ArrayList;
import java.util.List;

public class ZonaDeCarga {

    private final List<Pedido> pedidosPendientes;

    public ZonaDeCarga() {
        pedidosPendientes = new ArrayList<>();
        System.out.println("[Zona de carga inicializada]");
    }

    public synchronized void agregarPedido(Pedido pedido) {
        if (pedido == null) {
            return;
        }

        pedidosPendientes.add(pedido);

        System.out.println(
                "Pedido #" + pedido.getIdPedido()
                        + " agregado. Destino: "
                        + pedido.getDireccionEntrega()
        );
    }

    public synchronized Pedido retirarPedido() {
        if (pedidosPendientes.isEmpty()) {
            return null;
        }

        return pedidosPendientes.remove(0);
    }

    public synchronized int getCantidadPedidosPendientes() {
        return pedidosPendientes.size();
    }

    public synchronized boolean estaVacia() {
        return pedidosPendientes.isEmpty();
    }
}