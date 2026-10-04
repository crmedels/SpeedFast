package dao;

import cl.speedfast.modelo.Entrega;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona entregas y mantiene sincronizado el estado del pedido.
 */
public class EntregaDAO {

    /**
     * Guarda la entrega y cambia el pedido a EN_REPARTO
     * dentro de una misma transacción.
     */
    public boolean create(Entrega entrega) throws SQLException {

        validarEntrega(entrega);

        String sql = """
                INSERT INTO entrega
                    (id_pedido, id_repartidor, fecha, hora)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar()) {

            conexion.setAutoCommit(false);

            try {

                comprobarPedidoDisponible(
                        conexion,
                        entrega.getIdPedido()
                );

                int idGenerado;

                try (PreparedStatement sentencia =
                             conexion.prepareStatement(
                                     sql,
                                     Statement.RETURN_GENERATED_KEYS
                             )) {

                    cargarParametros(sentencia, entrega);

                    if (sentencia.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo registrar la entrega."
                        );
                    }

                    try (ResultSet claves =
                                 sentencia.getGeneratedKeys()) {

                        if (!claves.next()) {
                            throw new SQLException(
                                    "No se pudo obtener el ID de la entrega."
                            );
                        }

                        idGenerado = claves.getInt(1);
                    }
                }

                cambiarEstado(
                        conexion,
                        entrega.getIdPedido(),
                        "EN_REPARTO"
                );

                conexion.commit();
                entrega.setIdEntrega(idGenerado);

                return true;

            } catch (SQLException | IllegalArgumentException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public List<Entrega> readAll() throws SQLException {

        List<Entrega> entregas = new ArrayList<>();

        String sql = """
                SELECT id, id_pedido, id_repartidor, fecha, hora
                FROM entrega
                ORDER BY id
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                entregas.add(reconstruirEntrega(resultado));
            }
        }

        return entregas;
    }

    public boolean update(Entrega entrega) throws SQLException {

        validarEntrega(entrega);

        String sql = """
                UPDATE entrega
                SET id_pedido = ?, id_repartidor = ?,
                    fecha = ?, hora = ?
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar()) {

            conexion.setAutoCommit(false);

            try {

                Integer pedidoAnterior = obtenerPedidoDeEntrega(
                        conexion,
                        entrega.getIdEntrega()
                );

                if (pedidoAnterior == null) {
                    conexion.rollback();
                    return false;
                }

                bloquearPedido(conexion, pedidoAnterior);

                boolean cambiaPedido =
                        pedidoAnterior != entrega.getIdPedido();

                if (cambiaPedido) {
                    comprobarPedidoDisponible(
                            conexion,
                            entrega.getIdPedido()
                    );
                }

                try (PreparedStatement sentencia =
                             conexion.prepareStatement(sql)) {

                    cargarParametros(sentencia, entrega);
                    sentencia.setInt(5, entrega.getIdEntrega());
                    sentencia.executeUpdate();
                }

                if (cambiaPedido) {

                    restablecerPedidoSinEntrega(
                            conexion,
                            pedidoAnterior
                    );

                    cambiarEstado(
                            conexion,
                            entrega.getIdPedido(),
                            "EN_REPARTO"
                    );
                }

                conexion.commit();
                return true;

            } catch (SQLException | IllegalArgumentException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public boolean delete(int idEntrega) throws SQLException {

        String sql = "DELETE FROM entrega WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar()) {

            conexion.setAutoCommit(false);

            try {

                Integer idPedido = obtenerPedidoDeEntrega(
                        conexion,
                        idEntrega
                );

                if (idPedido == null) {
                    conexion.rollback();
                    return false;
                }

                bloquearPedido(conexion, idPedido);

                try (PreparedStatement sentencia =
                             conexion.prepareStatement(sql)) {

                    sentencia.setInt(1, idEntrega);
                    sentencia.executeUpdate();
                }

                restablecerPedidoSinEntrega(conexion, idPedido);

                conexion.commit();
                return true;

            } catch (SQLException | IllegalArgumentException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public Entrega buscarPorPedido(int idPedido) throws SQLException {

        String sql = """
                SELECT id, id_pedido, id_repartidor, fecha, hora
                FROM entrega
                WHERE id_pedido = ?
                ORDER BY id DESC
                LIMIT 1
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return reconstruirEntrega(resultado);
                }
            }
        }

        return null;
    }

    // Compatibilidad con el código de las semanas anteriores.
    public boolean guardar(Entrega entrega) {

        try {
            return create(entrega);

        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error al guardar la entrega.");
            e.printStackTrace();
            return false;
        }
    }

    private void validarEntrega(Entrega entrega) {

        if (entrega == null
                || entrega.getIdPedido() <= 0
                || entrega.getIdRepartidor() <= 0
                || entrega.getFecha() == null
                || entrega.getHora() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar pedido, repartidor, fecha y hora."
            );
        }
    }

    private void cargarParametros(
            PreparedStatement sentencia,
            Entrega entrega
    ) throws SQLException {

        sentencia.setInt(1, entrega.getIdPedido());
        sentencia.setInt(2, entrega.getIdRepartidor());
        sentencia.setDate(3, Date.valueOf(entrega.getFecha()));
        sentencia.setTime(4, Time.valueOf(entrega.getHora()));
    }

    private Entrega reconstruirEntrega(
            ResultSet resultado
    ) throws SQLException {

        Entrega entrega = new Entrega(
                resultado.getInt("id_pedido"),
                resultado.getInt("id_repartidor"),
                resultado.getDate("fecha").toLocalDate(),
                resultado.getTime("hora").toLocalTime()
        );

        entrega.setIdEntrega(resultado.getInt("id"));

        return entrega;
    }

    private Integer obtenerPedidoDeEntrega(
            Connection conexion,
            int idEntrega
    ) throws SQLException {

        String sql = """
                SELECT id_pedido
                FROM entrega
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idEntrega);

            try (ResultSet resultado = sentencia.executeQuery()) {

                return resultado.next()
                        ? resultado.getInt("id_pedido")
                        : null;
            }
        }
    }

    private String bloquearPedido(
            Connection conexion,
            int idPedido
    ) throws SQLException {

        String sql = """
                SELECT estado
                FROM pedido
                WHERE id = ?
                FOR UPDATE
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (!resultado.next()) {
                    throw new IllegalArgumentException(
                            "El pedido seleccionado ya no existe."
                    );
                }

                return resultado.getString("estado");
            }
        }
    }

    private void comprobarPedidoDisponible(
            Connection conexion,
            int idPedido
    ) throws SQLException {

        String estado = bloquearPedido(conexion, idPedido);

        if (!"PENDIENTE".equals(estado)) {

            throw new IllegalArgumentException(
                    "El pedido debe estar PENDIENTE para iniciar una entrega."
            );
        }

        String sql = """
                SELECT id
                FROM entrega
                WHERE id_pedido = ?
                LIMIT 1
                FOR UPDATE
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    throw new IllegalArgumentException(
                            "El pedido ya tiene una entrega asociada."
                    );
                }
            }
        }
    }

    private void cambiarEstado(
            Connection conexion,
            int idPedido,
            String estado
    ) throws SQLException {

        String sql = """
                UPDATE pedido
                SET estado = ?
                WHERE id = ?
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setInt(2, idPedido);

            if (sentencia.executeUpdate() != 1) {
                throw new SQLException(
                        "No se pudo actualizar el estado del pedido."
                );
            }
        }
    }

    /**
     * Un pedido en reparto vuelve a pendiente si queda sin entregas.
     * Los pedidos entregados conservan su estado.
     */
    private void restablecerPedidoSinEntrega(
            Connection conexion,
            int idPedido
    ) throws SQLException {

        String sql = """
                UPDATE pedido
                SET estado = 'PENDIENTE'
                WHERE id = ?
                  AND estado = 'EN_REPARTO'
                  AND NOT EXISTS (
                      SELECT 1 FROM entrega
                      WHERE id_pedido = ?
                  )
                """;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);
            sentencia.setInt(2, idPedido);
            sentencia.executeUpdate();
        }
    }
}