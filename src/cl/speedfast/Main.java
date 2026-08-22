package cl.speedfast;

public class Main {

    public static void main(String[] args) {

        Pedido[] pedidos = {
                new PedidoComida(1, "Avenida Central 123", 4),
                new PedidoEncomienda(2, "Los Robles 456", 6),
                new PedidoExpress(3, "Las Flores 789", 7)
        };

        System.out.println("=== RESUMEN DE PEDIDOS Y TIEMPOS DE ENTREGA ===");
        System.out.println();

        for (Pedido pedido : pedidos) {
            pedido.mostrarResumen();
            System.out.println("Tiempo estimado de entrega: "
                    + pedido.calcularTiempoEntrega() + " minutos");
            System.out.println();
        }
    }
}