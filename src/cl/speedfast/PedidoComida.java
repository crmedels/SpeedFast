package cl.speedfast;

public class PedidoComida extends Pedido {

    public PedidoComida(int idPedido, String direccionEntrega, String tipoPedido) {
        super(idPedido, direccionEntrega, tipoPedido);
    }

    @Override
    public void asignarRepartidor() {
        System.out.println("[Pedido Comida]");
        System.out.println("Asignando repartidor...");
        System.out.println("Verificando mochila térmica... OK");
    }
}