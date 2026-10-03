package cl.speedfast.modelo;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Representa un repartidor de SpeedFast.
 * Permite trabajar con la base de datos y con la zona de carga.
 */
public class Repartidor implements Runnable {

    private int idRepartidor;
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

    public Repartidor(int idRepartidor, String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del repartidor no puede estar vacío."
            );
        }

        this.idRepartidor = idRepartidor;
        this.nombre = nombre.trim();
        this.zonaDeCarga = null;
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return idRepartidor + " - " + nombre;
    }

    @Override
    public void run() {

        if (zonaDeCarga == null) {
            throw new IllegalStateException(
                    "El repartidor necesita una zona de carga para ejecutarse."
            );
        }

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