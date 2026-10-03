package dao;

import cl.speedfast.modelo.EstadoPedido;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.PedidoComida;
import cl.speedfast.modelo.PedidoEncomienda;
import cl.speedfast.modelo.PedidoExpress;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gestiona las operaciones CRUD de pedidos mediante JDBC.
 */
public class PedidoDAO {

    public boolean create(Pedido pedido) throws SQLException {

        String sql = """
                INSERT INTO pedido (direccion, tipo, estado)
                VALUES (?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            sentencia.setString(1, pedido.getDireccionEntrega());
            sentencia.setString(2, obtenerTipoPedido(pedido));
            sentencia.setString(3, pedido.getEstado().name());

            if (sentencia.executeUpdate() == 0) {
                return false;
            }

            try (ResultSet claves = sentencia.getGeneratedKeys()) {

                if (claves.next()) {
                    pedido.setIdPedido(claves.getInt(1));
                }
            }

            return true;
        }
    }

    /**
     * Reconstruye las subclases y restaura el estado persistido.
     * La distancia no se almacena en el esquema actual.
     */
    public List<Pedido> readAll() throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql = """
                SELECT id, direccion, tipo, estado
                FROM pedido
                ORDER BY id
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {

                int id = resultado.getInt("id");
                String direccion = resultado.getString("direccion");

                String tipo = resultado.getString("tipo")
                        .trim().toUpperCase(Locale.ROOT);

                EstadoPedido estado = EstadoPedido.valueOf(
                        resultado.getString("estado")
                                .trim().toUpperCase(Locale.ROOT)
                );

                Pedido pedido = switch (tipo) {

                    case "COMIDA" ->
                            new PedidoComida(id, direccion, 0);

                    case "ENCOMIENDA" ->
                            new PedidoEncomienda(id, direccion, 0);

                    case "EXPRESS" ->
                            new PedidoExpress(id, direccion, 0);

                    default -> throw new IllegalArgumentException(
                            "Tipo de pedido no reconocido: " + tipo
                    );
                };

                if (estado != EstadoPedido.PENDIENTE) {
                    pedido.setEstado(estado);
                }

                pedidos.add(pedido);
            }
        }

        return pedidos;
    }

    public boolean update(Pedido pedido) throws SQLException {

        String sql = """
                UPDATE pedido
                SET direccion = ?, tipo = ?, estado = ?
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, pedido.getDireccionEntrega());
            sentencia.setString(2, obtenerTipoPedido(pedido));
            sentencia.setString(3, pedido.getEstado().name());
            sentencia.setInt(4, pedido.getIdPedido());

            return sentencia.executeUpdate() > 0;
        }
    }

    /**
     * La clave foránea impide eliminar pedidos con entregas asociadas.
     */
    public boolean delete(int idPedido) throws SQLException {

        String sql = """
                DELETE FROM pedido
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            return sentencia.executeUpdate() > 0;
        }
    }

    // Compatibilidad con las ventanas existentes.
    public boolean guardar(Pedido pedido) {

        try {
            return create(pedido);

        } catch (SQLException e) {
            System.err.println("Error al guardar el pedido.");
            e.printStackTrace();
            return false;
        }
    }

    public List<Pedido> listarPedidos() {

        try {
            return readAll();

        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error al recuperar los pedidos.");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Object[]> listarTodos() {

        List<Object[]> filas = new ArrayList<>();

        for (Pedido pedido : listarPedidos()) {

            filas.add(
                    new Object[]{
                            pedido.getIdPedido(),
                            obtenerTipoPedido(pedido),
                            pedido.getDireccionEntrega(),
                            pedido.getEstado().name()
                    }
            );
        }

        return filas;
    }

    public boolean actualizarEstado(
            int idPedido,
            EstadoPedido estado
    ) {

        String sql = """
                UPDATE pedido
                SET estado = ?
                WHERE id = ?
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado.name());
            sentencia.setInt(2, idPedido);

            return sentencia.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado del pedido.");
            e.printStackTrace();
            return false;
        }
    }

    public String obtenerTipoPedido(Pedido pedido) {

        if (pedido instanceof PedidoComida) {
            return "COMIDA";
        }

        if (pedido instanceof PedidoEncomienda) {
            return "ENCOMIENDA";
        }

        if (pedido instanceof PedidoExpress) {
            return "EXPRESS";
        }

        throw new IllegalArgumentException(
                "Tipo de pedido no reconocido."
        );
    }
}