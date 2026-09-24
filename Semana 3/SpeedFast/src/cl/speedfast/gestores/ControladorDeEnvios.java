package cl.speedfast.gestores;

import cl.speedfast.Pedido;
import cl.speedfast.interfaces.Cancelable;
import cl.speedfast.interfaces.Despachable;
import cl.speedfast.interfaces.Rastreable;

import java.util.ArrayList;

public class ControladorDeEnvios {

    private final ArrayList<Rastreable> pedidosRegistrados;

    public ControladorDeEnvios() {
        pedidosRegistrados = new ArrayList<>();
    }

    public void registrarPedido(Pedido pedido) {
        pedidosRegistrados.add(pedido);
    }

    public void reservarPedido(Pedido pedido) {
        pedido.reservar();
    }

    public void despacharPedido(Despachable pedido) {
        pedido.despachar();
    }

    public void cancelarPedido(Cancelable pedido) {
        pedido.cancelar();
    }

    public void mostrarHistorial(Rastreable pedido) {
        pedido.verHistorial();
    }

    public void mostrarTodosLosHistoriales() {
        System.out.println("=== HISTORIAL DE PEDIDOS ===");
        System.out.println();

        for (Rastreable pedido : pedidosRegistrados) {
            pedido.verHistorial();
            System.out.println();
        }
    }
}