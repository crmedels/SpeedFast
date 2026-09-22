package cl.speedfast;

import cl.speedfast.gestores.ControladorDeEnvios;

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

        // Registro de todos los pedidos para mantener su historial.
        for (Pedido pedido : pedidos) {
            controlador.registrarPedido(pedido);
        }

        System.out.println("==============================================");
        System.out.println("       SISTEMA DE ENTREGAS SPEEDFAST");
        System.out.println("==============================================");
        System.out.println();

        // Recurso compartido por todos los repartidores.
        ZonaDeCarga zonaDeCarga = new ZonaDeCarga();

        System.out.println();
        System.out.println("=== CARGA DE PEDIDOS ===");
        System.out.println();

        for (Pedido pedido : pedidos) {
            zonaDeCarga.agregarPedido(pedido);
        }

        System.out.println();
        System.out.println(
                "Pedidos pendientes en zona de carga: "
                        + zonaDeCarga.getCantidadPedidosPendientes()
        );

        System.out.println();
        System.out.println("=== RESUMEN INICIAL DE PEDIDOS ===");
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

        // Los tres repartidores comparten la misma zona de carga.
        Repartidor repartidor1 =
                new Repartidor("Camila", zonaDeCarga);

        Repartidor repartidor2 =
                new Repartidor("Luis", zonaDeCarga);

        Repartidor repartidor3 =
                new Repartidor("Daniela", zonaDeCarga);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.execute(repartidor1);
        executor.execute(repartidor2);
        executor.execute(repartidor3);

        // No se aceptan nuevas tareas.
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
        System.out.println("=== ESTADO FINAL DE LOS PEDIDOS ===");
        System.out.println();

        boolean todosEntregados = true;

        for (Pedido pedido : pedidos) {

            System.out.println(
                    "Pedido #" + pedido.getIdPedido()
                            + " | Repartidor: "
                            + pedido.getNombreRepartidor()
                            + " | Estado: "
                            + pedido.getEstado()
            );

            if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
                todosEntregados = false;
            }
        }

        System.out.println();

        if (todosEntregados) {
            System.out.println(
                    "Todos los pedidos han sido entregados correctamente"
            );
        } else {
            System.out.println(
                    "Algunos pedidos no pudieron ser entregados."
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