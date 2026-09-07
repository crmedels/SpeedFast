package cl.speedfast;

public class PedidoEncomienda extends Pedido {

    private final int tiempoBase = 20;
    private final double minutosPorKm = 1.5;

    public PedidoEncomienda(int idPedido, String direccionEntrega, int distanciaKm) {
        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public int calcularTiempoEntrega() {
        return (int) Math.round(
                tiempoBase + (minutosPorKm * getDistanciaKm())
        );
    }

    @Override
    public void asignarRepartidor() {
        String nombreRepartidor = "Daniela Tapia";

        registrarRepartidor(nombreRepartidor);

        System.out.println("[Pedido Encomienda]");
        System.out.println("Validando peso y embalaje... OK");
        System.out.println("Repartidor asignado automáticamente: "
                + nombreRepartidor + ".");
    }

    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        registrarRepartidor(nombreRepartidor);

        System.out.println("[Pedido Encomienda]");
        System.out.println("Validando peso y embalaje... OK");
        System.out.println("Pedido asignado manualmente a "
                + nombreRepartidor + ".");
    }
}