package cl.speedfast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Representa a un repartidor que procesa una lista de pedidos
 * de forma independiente mediante un hilo de ejecución.
 */
public class Repartidor implements Runnable {

    private final String nombre;
    private final List<Pedido> pedidosAsignados;

    public Repartidor(String nombre, List<Pedido> pedidosAsignados) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del repartidor no puede estar vacío."
            );
        }

        if (pedidosAsignados == null || pedidosAsignados.isEmpty()) {
            throw new IllegalArgumentException(
                    "La lista de pedidos no puede estar vacía."
            );
        }

        for (Pedido pedido : pedidosAsignados) {
            if (pedido == null) {
                throw new IllegalArgumentException(
                        "La lista no puede contener pedidos nulos."
                );
            }
        }

        this.nombre = nombre.trim();
        this.pedidosAsignados = new ArrayList<>(pedidosAsignados);

        // Asigna este repartidor a todos sus pedidos.
        for (Pedido pedido : this.pedidosAsignados) {
            pedido.asignarRepartidor(this.nombre);
        }
    }

    @Override
    public void run() {

        for (Pedido pedido : pedidosAsignados) {

            System.out.println(
                    "[Repartidor: " + nombre + "] Entregando "
                            + pedido.getClass().getSimpleName()
                            + " #" + pedido.getIdPedido() + "..."
            );

            try {
                int tiempoEspera =
                        ThreadLocalRandom.current().nextInt(1000, 3001);

                Thread.sleep(tiempoEspera);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                System.out.println(
                        "[Repartidor: " + nombre
                                + "] Entrega interrumpida."
                );

                return;
            }

            System.out.println(
                    "[Repartidor: " + nombre + "] Pedido #"
                            + pedido.getIdPedido() + " entregado."
            );
        }

        System.out.println(
                "[Repartidor: " + nombre
                        + "] Todas las entregas finalizadas."
        );
    }
}