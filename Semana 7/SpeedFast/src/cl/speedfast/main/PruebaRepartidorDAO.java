package cl.speedfast.main;

import cl.speedfast.modelo.Repartidor;
import dao.RepartidorDAO;

import java.util.List;

public class PruebaRepartidorDAO {

    public static void main(String[] args) {

        RepartidorDAO repartidorDAO = new RepartidorDAO();

        Repartidor nuevoRepartidor =
                new Repartidor(0, "Carlos");

        if (repartidorDAO.guardar(nuevoRepartidor)) {

            System.out.println(
                    "Repartidor guardado correctamente."
            );

            System.out.println(
                    "ID generado: "
                            + nuevoRepartidor.getIdRepartidor()
            );

        } else {

            System.out.println(
                    "No se pudo guardar el repartidor."
            );
        }

        System.out.println();
        System.out.println("REPARTIDORES REGISTRADOS");

        List<Repartidor> repartidores =
                repartidorDAO.listarTodos();

        for (Repartidor repartidor : repartidores) {

            System.out.println(
                    "ID: "
                            + repartidor.getIdRepartidor()
                            + " | Nombre: "
                            + repartidor.getNombre()
            );
        }
    }
}