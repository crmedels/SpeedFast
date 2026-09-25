package cl.speedfast.main;

import cl.speedfast.modelo.Entrega;
import dao.EntregaDAO;

import java.time.LocalDate;
import java.time.LocalTime;

public class PruebaEntregaDAO {

    public static void main(String[] args) {

        Entrega entrega = new Entrega(
                1,
                1,
                LocalDate.now(),
                LocalTime.now()
        );

        EntregaDAO entregaDAO = new EntregaDAO();

        if (entregaDAO.guardar(entrega)) {

            System.out.println("Entrega guardada correctamente.");
            System.out.println(
                    "ID generado: " + entrega.getIdEntrega()
            );

        } else {

            System.out.println("No se pudo guardar la entrega.");
        }
    }
}