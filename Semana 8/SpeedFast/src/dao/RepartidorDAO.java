package dao;

import cl.speedfast.modelo.Repartidor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la persistencia de repartidores mediante JDBC.
 */
public class RepartidorDAO {

    public boolean create(Repartidor repartidor) throws SQLException {

        String sql = """
                INSERT INTO repartidor (nombre)
                VALUES (?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            sentencia.setString(1, repartidor.getNombre());

            if (sentencia.executeUpdate() == 0) {
                return false;
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {

                if (claves.next()) {
                    repartidor.setIdRepartidor(claves.getInt(1));
                }
            }

            return true;
        }
    }

    public List<Repartidor> readAll() throws SQLException {

        List<Repartidor> repartidores = new ArrayList<>();

        String sql = """
                SELECT id, nombre
                FROM repartidor
                ORDER BY id
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {

                repartidores.add(
                        new Repartidor(
                                resultado.getInt("id"),
                                resultado.getString("nombre")
                        )
                );
            }
        }

        return repartidores;
    }

    public boolean update(Repartidor repartidor) throws SQLException {

        String sql = """
                UPDATE repartidor
                SET nombre = ?
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, repartidor.getNombre());
            sentencia.setInt(2, repartidor.getIdRepartidor());

            return sentencia.executeUpdate() > 0;
        }
    }

    /**
     * MySQL impide eliminar repartidores asociados a entregas.
     * La ventana muestra el motivo al usuario.
     */
    public boolean delete(int idRepartidor) throws SQLException {

        String sql = """
                DELETE FROM repartidor
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idRepartidor);

            return sentencia.executeUpdate() > 0;
        }
    }

    // Mantiene compatibles las ventanas de las semanas anteriores.
    public boolean guardar(Repartidor repartidor) {

        try {
            return create(repartidor);

        } catch (SQLException e) {
            System.err.println("Error al guardar el repartidor.");
            e.printStackTrace();
            return false;
        }
    }

    public List<Repartidor> listarTodos() {

        try {
            return readAll();

        } catch (SQLException e) {
            System.err.println("Error al listar los repartidores.");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Repartidor buscarPorId(int idRepartidor) {

        String sql = """
                SELECT id, nombre
                FROM repartidor
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idRepartidor);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    return new Repartidor(
                            resultado.getInt("id"),
                            resultado.getString("nombre")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar el repartidor.");
            e.printStackTrace();
        }

        return null;
    }
}