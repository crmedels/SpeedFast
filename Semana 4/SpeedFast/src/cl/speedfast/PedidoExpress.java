package cl.speedfast;

public class PedidoExpress extends Pedido {

    private final int tiempoBase = 10;
    private final int distanciaLimite = 5;
    private final int tiempoExtra = 5;

    public PedidoExpress(int idPedido, String direccionEntrega, int distanciaKm) {
        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public int calcularTiempoEntrega() {
        if (getDistanciaKm() > distanciaLimite) {
            return tiempoBase + tiempoExtra;
        }

        return tiempoBase;
    }

    @Override
    public void asignarRepartidor() {
        String nombreRepartidor = "Repartidor Express";

        registrarRepartidor(nombreRepartidor);

        System.out.println("[Pedido Express]");
        System.out.println("Buscando repartidor más cercano...");
        System.out.println("Repartidor asignado automáticamente: "
                + nombreRepartidor + ".");
    }

    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        registrarRepartidor(nombreRepartidor);

        System.out.println("[Pedido Express]");
        System.out.println("Asignación express manual.");
        System.out.println("Pedido asignado manualmente a "
                + nombreRepartidor + ".");
    }
}