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
        System.out.println("[Pedido Express]");
        System.out.println("Asignando repartidor...");
        System.out.println("Repartidor más cercano con disponibilidad inmediata encontrado.");
    }

    @Override
    public void asignarRepartidor(String nombreRepartidor) {
        System.out.println("[Pedido Express]");
        System.out.println("Asignando repartidor...");
        System.out.println("Repartidor más cercano con disponibilidad inmediata encontrado.");
        System.out.println("Pedido asignado a " + nombreRepartidor + ".");
    }
}