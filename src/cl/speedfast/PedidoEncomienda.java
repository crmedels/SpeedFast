package cl.speedfast;

public class PedidoEncomienda extends Pedido {

    private final int tiempoBase = 20;
    private final double minutosPorKm = 1.5;

    public PedidoEncomienda(int idPedido, String direccionEntrega, int distanciaKm) {
        super(idPedido, direccionEntrega, distanciaKm);
    }

    @Override
    public int calcularTiempoEntrega() {
        return (int) Math.round(tiempoBase + (minutosPorKm * getDistanciaKm()));
    }

    @Override
    public void asignarRepartidor() {
        System.out.println("[Pedido Encomienda]");
        System.out.println("Asignando repartidor...");
        System.out.println("Validando peso y embalaje... OK");
    }

    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        System.out.println("[Pedido Encomienda]");
        System.out.println("Asignando repartidor...");
        System.out.println("Validando peso y embalaje... OK");
        System.out.println("Pedido asignado a " + nombreRepartidor + ".");
    }
}