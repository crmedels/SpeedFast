package cl.speedfast;

public class Main {

    public static void main(String[] args) {

        PedidoComida pedidoComida =
                new PedidoComida(1, "Avenida Central 123", "Comida");

        PedidoEncomienda pedidoEncomienda =
                new PedidoEncomienda(2, "Los Robles 456", "Encomienda");

        PedidoExpress pedidoExpress =
                new PedidoExpress(3, "Las Flores 789", "Express");

        System.out.println("=== ASIGNACIÓN MEDIANTE POLIMORFISMO ===");
        System.out.println();

        Pedido[] pedidos = {
                pedidoComida,
                pedidoEncomienda,
                pedidoExpress
        };

        for (Pedido pedido : pedidos) {
            pedido.asignarRepartidor();
            System.out.println();
        }

        System.out.println("=== ASIGNACIÓN CON NOMBRE DE REPARTIDOR ===");
        System.out.println();

        String[] nombresRepartidores = {
                "Juan Pérez",
                "Camila Soto",
                "Luis Díaz"
        };

        for (int i = 0; i < pedidos.length; i++) {
            pedidos[i].asignarRepartidor(nombresRepartidores[i]);
            System.out.println();
        }
    }
}