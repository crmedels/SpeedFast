package cl.speedfast;

import cl.speedfast.gestores.ControladorDeEnvios;

public class Main {

    public static void main(String[] args) {

        ControladorDeEnvios controlador = new ControladorDeEnvios();

        PedidoComida pedidoComida =
                new PedidoComida(101, "Avenida Central 123", 4);

        PedidoEncomienda pedidoEncomienda =
                new PedidoEncomienda(102, "Los Robles 456", 6);

        PedidoExpress pedidoExpress =
                new PedidoExpress(103, "Las Flores 789", 7);

        Pedido[] pedidos = {
                pedidoComida,
                pedidoEncomienda,
                pedidoExpress
        };

        // Registro de los pedidos en el controlador.
        for (Pedido pedido : pedidos) {
            controlador.registrarPedido(pedido);
        }

        System.out.println("==============================================");
        System.out.println("       SISTEMA DE ENTREGAS SPEEDFAST");
        System.out.println("==============================================");
        System.out.println();

        // Demostración polimórfica de resumen y cálculo de tiempo.
        System.out.println("=== RESUMEN Y TIEMPOS DE ENTREGA ===");
        System.out.println();

        for (Pedido pedido : pedidos) {
            pedido.mostrarResumen();
            System.out.println("Tiempo estimado de entrega: "
                    + pedido.calcularTiempoEntrega() + " minutos");
            System.out.println();
        }

        // CASO 1: Pedido de comida.
        System.out.println("=== CASO 1: PEDIDO DE COMIDA ===");
        pedidoComida.asignarRepartidor();
        System.out.println("Tiempo estimado: "
                + pedidoComida.calcularTiempoEntrega() + " minutos");

        controlador.reservarPedido(pedidoComida);
        controlador.despacharPedido(pedidoComida);

        System.out.println();
        controlador.mostrarHistorial(pedidoComida);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println();

        // CASO 2: Pedido de encomienda.
        System.out.println("=== CASO 2: PEDIDO DE ENCOMIENDA ===");
        pedidoEncomienda.asignarRepartidor("Daniela Tapia");
        System.out.println("Tiempo estimado: "
                + pedidoEncomienda.calcularTiempoEntrega() + " minutos");

        controlador.reservarPedido(pedidoEncomienda);
        controlador.despacharPedido(pedidoEncomienda);

        System.out.println("Intentando cancelar el pedido después del despacho:");
        controlador.cancelarPedido(pedidoEncomienda);

        System.out.println();
        controlador.mostrarHistorial(pedidoEncomienda);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println();

        // CASO 3: Pedido express.
        System.out.println("=== CASO 3: PEDIDO EXPRESS ===");
        pedidoExpress.asignarRepartidor();
        System.out.println("Tiempo estimado: "
                + pedidoExpress.calcularTiempoEntrega() + " minutos");

        controlador.reservarPedido(pedidoExpress);
        controlador.cancelarPedido(pedidoExpress);

        System.out.println();
        controlador.mostrarHistorial(pedidoExpress);

        System.out.println();
        System.out.println("==============================================");
        controlador.mostrarTodosLosHistoriales();
    }
}