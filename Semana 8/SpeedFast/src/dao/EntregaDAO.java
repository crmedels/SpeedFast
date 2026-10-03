package dao;

import cl.speedfast.modelo.Entrega;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EntregaDAO {

    public boolean guardar(Entrega entrega) {

        String sql = """
                INSERT INTO entrega (
                    id_pedido,
                    id_repartidor,
                    fecha,
                    hora
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            sentencia.setInt(
                    1,
                    entrega.getIdPedido()
            );

            sentencia.setInt(
                    2,
                    entrega.getIdRepartidor()
            );

            sentencia.setDate(
                    3,
                    java.sql.Date.valueOf(entrega.getFecha())
            );

            sentencia.setTime(
                    4,
                    java.sql.Time.valueOf(entrega.getHora())
            );

            int filasAfectadas = sentencia.executeUpdate();

            if (filasAfectadas > 0) {

                try (ResultSet clavesGeneradas =
                             sentencia.getGeneratedKeys()) {

                    if (clavesGeneradas.next()) {
                        entrega.setIdEntrega(
                                clavesGeneradas.getInt(1)
                        );
                    }
                }

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error al guardar la entrega en la base de datos."
            );

            e.printStackTrace();
        }

        return false;
    }
}