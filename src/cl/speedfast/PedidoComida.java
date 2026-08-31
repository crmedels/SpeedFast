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
        String nombreRepartidor = "Luis Díaz";

        registrarRepartidor(nombreRepartidor);

        System.out.println("[Pedido Comida]");
        System.out.println("Verificando mochila térmica... OK");
        System.out.println("Repartidor asignado automáticamente: "
                + nombreRepartidor + ".");
    }

    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        registrarRepartidor(nombreRepartidor);

        System.out.println("[Pedido Comida]");
        System.out.println("Verificando mochila térmica... OK");
        System.out.println("Pedido asignado manualmente a "
                + nombreRepartidor + ".");
    }
}