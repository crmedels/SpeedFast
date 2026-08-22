package cl.speedfast;

public class PedidoComida extends Pedido {

    private final int tiempoBase = 15;
    private final int minutosPorKm = 2;

    public PedidoComida(int idPedido, String direccionEntrega, int distanciaKm) {
        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public int calcularTiempoEntrega() {
        return tiempoBase + (minutosPorKm * getDistanciaKm());
    }

    @Override
    public void asignarRepartidor() {
        System.out.println("[Pedido Comida]");
        System.out.println("Asignando repartidor...");
        System.out.println("Verificando mochila térmica... OK");
    }

    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        System.out.println("[Pedido Comida]");
        System.out.println("Asignando repartidor...");
        System.out.println("Verificando mochila térmica... OK");
        System.out.println("Pedido asignado a " + nombreRepartidor + ".");
    }
}