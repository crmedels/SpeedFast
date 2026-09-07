package cl.speedfast;

import cl.speedfast.gestores.ControladorDeEnvios;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        ControladorDeEnvios controlador = new ControladorDeEnvios();

        Pedido[] pedidos = {
                new PedidoComida(101, "Avenida Central 123", 4),
                new PedidoEncomienda(102, "Los Robles 456", 6),
                new PedidoExpress(103, "Las Flores 789", 7),
                new PedidoComida(104, "Los Aromos 321", 3),
                new PedidoEncomienda(105, "Avenida Norte 654", 8),
                new PedidoExpress(106, "Los Castaños 987", 5)
        };

        // Registro de todos los pedidos mediante referencias de tipo Pedido.
        for (Pedido pedido : pedidos) {
            controlador.registrarPedido(pedido);
        }

        System.out.println("==============================================");
        System.out.println("       SISTEMA DE ENTREGAS SPEEDFAST");
        System.out.println("==============================================");
        System.out.println();

        System.out.println("=== ASIGNACIÓN DE PEDIDOS ===");
        System.out.println();

        Repartidor repartidor1 = new Repartidor(
                "Camila",
                List.of(pedidos[0], pedidos[1])
        );

        System.out.println();

        Repartidor repartidor2 = new Repartidor(
                "Luis",
                List.of(pedidos[2], pedidos[3])
        );

        System.out.println();

        Repartidor repartidor3 = new Repartidor(
                "Daniela",
                List.of(pedidos[4], pedidos[5])
        );

        System.out.println();
        System.out.println("=== RESUMEN DE PEDIDOS ASIGNADOS ===");
        System.out.println();

        for (Pedido pedido : pedidos) {
            pedido.mostrarResumen();

            System.out.println(
                    "Tiempo estimado de entrega: "
                            + pedido.calcularTiempoEntrega()
                            + " minutos"
            );

            System.out.println();
        }

        System.out.println("=== INICIO DE ENTREGAS CONCURRENTES ===");
        System.out.println();

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.execute(repartidor1);
        executor.execute(repartidor2);
        executor.execute(repartidor3);

        // No se aceptan nuevas tareas, pero las actuales pueden finalizar.
        executor.shutdown();

        try {

            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {

                System.out.println(
                        "La simulación excedió el tiempo máximo de espera."
                );

                executor.shutdownNow();
            }

        } catch (InterruptedException e) {

            executor.shutdownNow();
            Thread.currentThread().interrupt();

            System.out.println(
                    "La ejecución principal fue interrumpida."
            );
        }

        System.out.println();
        System.out.println("==============================================");
        System.out.println("       SIMULACIÓN SPEEDFAST FINALIZADA");
        System.out.println("==============================================");
        System.out.println();

        controlador.mostrarTodosLosHistoriales();
    }
}