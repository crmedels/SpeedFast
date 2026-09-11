package cl.speedfast;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Representa a un repartidor que retira pedidos desde una zona
 * de carga compartida y realiza las entregas de forma concurrente.
 */
public class Repartidor implements Runnable {

    private final String nombre;
    private final ZonaDeCarga zonaDeCarga;

    public Repartidor(String nombre, ZonaDeCarga zonaDeCarga) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del repartidor no puede estar vacío."
            );
        }

        if (zonaDeCarga == null) {
            throw new IllegalArgumentException(
                    "La zona de carga no puede ser nula."
            );
        }

        this.nombre = nombre.trim();
        this.zonaDeCarga = zonaDeCarga;
    }

    @Override
    public void run() {

        while (true) {

            Pedido pedido = zonaDeCarga.retirarPedido();

            if (pedido == null) {
                break;
            }

            pedido.asignarRepartidor(nombre);
            pedido.setEstado(EstadoPedido.EN_REPARTO);

            System.out.println(
                    "[Repartidor - " + nombre + "] Retirando pedido #"
                            + pedido.getIdPedido() + "..."
            );

            System.out.println(
                    "[Repartidor - " + nombre + "] Estado: "
                            + pedido.getEstado()
            );

            try {

                int tiempoEspera =
                        ThreadLocalRandom.current().nextInt(1000, 3001);

                System.out.println(
                        "[Repartidor - " + nombre + "] Entregando pedido #"
                                + pedido.getIdPedido() + "..."
                );

                Thread.sleep(tiempoEspera);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                System.out.println(
                        "[Repartidor - " + nombre
                                + "] Entrega interrumpida."
                );

                return;
            }

            pedido.setEstado(EstadoPedido.ENTREGADO);

            System.out.println(
                    "[Repartidor - " + nombre + "] Pedido #"
                            + pedido.getIdPedido() + " entregado."
            );

            System.out.println(
                    "[Repartidor - " + nombre + "] Estado: "
                            + pedido.getEstado()
            );
        }

        System.out.println(
                "[Repartidor - " + nombre
                        + "] No quedan pedidos pendientes."
        );
    }
}