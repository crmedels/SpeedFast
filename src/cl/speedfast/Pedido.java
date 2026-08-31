package cl.speedfast;

import cl.speedfast.interfaces.Cancelable;
import cl.speedfast.interfaces.Despachable;
import cl.speedfast.interfaces.Rastreable;

import java.util.ArrayList;

public abstract class Pedido implements Despachable, Cancelable, Rastreable {

    private int idPedido;
    private String direccionEntrega;
    private int distanciaKm;

    private boolean reservado;
    private boolean despachado;
    private boolean cancelado;

    private final ArrayList<String> historial;

    public Pedido(int idPedido, String direccionEntrega, int distanciaKm) {
        this.idPedido = idPedido;
        this.direccionEntrega = direccionEntrega;
        this.distanciaKm = distanciaKm;

        this.reservado = false;
        this.despachado = false;
        this.cancelado = false;

        this.historial = new ArrayList<>();
        historial.add("Pedido creado.");
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public int getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(int distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public void mostrarResumen() {
        System.out.println("Tipo de entrega: " + getClass().getSimpleName());
        System.out.println("ID del pedido: " + idPedido);
        System.out.println("Dirección de entrega: " + direccionEntrega);
        System.out.println("Distancia: " + distanciaKm + " km");
    }

    public abstract int calcularTiempoEntrega();

    public void asignarRepartidor() {
        System.out.println("Asignando repartidor para el pedido...");
    }

    public void asignarRepartidor(String nombreRepartidor) {
        System.out.println("Pedido asignado a " + nombreRepartidor + ".");
    }

    public void reservar() {
        if (cancelado) {
            System.out.println("No se puede reservar el pedido porque está cancelado.");
            return;
        }

        if (reservado) {
            System.out.println("El pedido ya se encuentra reservado.");
            return;
        }

        reservado = true;
        historial.add("Pedido reservado.");
        System.out.println("Pedido #" + idPedido + " reservado correctamente.");
    }

    @Override
    public void despachar() {
        if (cancelado) {
            System.out.println("No se puede despachar el pedido porque está cancelado.");
            return;
        }

        if (!reservado) {
            System.out.println("No se puede despachar el pedido porque no está reservado.");
            return;
        }

        if (despachado) {
            System.out.println("El pedido ya fue despachado.");
            return;
        }

        despachado = true;
        historial.add("Pedido despachado.");
        System.out.println("Pedido #" + idPedido + " despachado correctamente.");
    }

    @Override
    public void cancelar() {
        if (despachado) {
            System.out.println("No se puede cancelar el pedido porque ya fue despachado.");
            return;
        }

        if (cancelado) {
            System.out.println("El pedido ya se encuentra cancelado.");
            return;
        }

        cancelado = true;
        historial.add("Pedido cancelado.");
        System.out.println("Pedido #" + idPedido + " cancelado correctamente.");
    }

    @Override
    public void verHistorial() {
        System.out.println("Historial del pedido #" + idPedido + ":");

        for (String registro : historial) {
            System.out.println("- " + registro);
        }
    }
}