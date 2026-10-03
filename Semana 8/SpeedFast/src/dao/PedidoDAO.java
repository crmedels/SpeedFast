package dao;

import cl.speedfast.modelo.EstadoPedido;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.PedidoComida;
import cl.speedfast.modelo.PedidoEncomienda;
import cl.speedfast.modelo.PedidoExpress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public boolean guardar(Pedido pedido) {

        String sql = """
                INSERT INTO pedido (direccion, tipo, estado)
                VALUES (?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            sentencia.setString(
                    1,
                    pedido.getDireccionEntrega()
            );

            sentencia.setString(
                    2,
                    obtenerTipoPedido(pedido)
            );

            sentencia.setString(
                    3,
                    pedido.getEstado().name()
            );

            int filasAfectadas = sentencia.executeUpdate();

            if (filasAfectadas > 0) {

                try (ResultSet clavesGeneradas =
                             sentencia.getGeneratedKeys()) {

                    if (clavesGeneradas.next()) {
                        pedido.setIdPedido(
                                clavesGeneradas.getInt(1)
                        );
                    }
                }

                return true;
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error al guardar el pedido en la base de datos."
            );

            e.printStackTrace();
        }

        return false;
    }

    public List<Object[]> listarTodos() {

        List<Object[]> pedidos = new ArrayList<>();

        String sql = """
                SELECT id, tipo, direccion, estado
                FROM pedido
                ORDER BY id
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {

                Object[] pedido = {
                        resultado.getInt("id"),
                        resultado.getString("tipo"),
                        resultado.getString("direccion"),
                        resultado.getString("estado")
                };

                pedidos.add(pedido);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error al listar los pedidos de la base de datos."
            );

            e.printStackTrace();
        }

        return pedidos;
    }

    public List<Pedido> listarPedidos() {

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
                String direccion =
                        resultado.getString("direccion");

                String tipo =
                        resultado.getString("tipo");

                EstadoPedido estado =
                        EstadoPedido.valueOf(
                                resultado
                                        .getString("estado")
                                        .trim()
                                        .toUpperCase()
                        );

                Pedido pedido;

                switch (tipo.trim().toUpperCase()) {

                    case "COMIDA":
                        pedido = new PedidoComida(
                                id,
                                direccion,
                                0
                        );
                        break;

                    case "ENCOMIENDA":
                        pedido = new PedidoEncomienda(
                                id,
                                direccion,
                                0
                        );
                        break;

                    case "EXPRESS":
                        pedido = new PedidoExpress(
                                id,
                                direccion,
                                0
                        );
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Tipo de pedido no reconocido: "
                                        + tipo
                        );
                }

                if (estado != EstadoPedido.PENDIENTE) {
                    pedido.setEstado(estado);
                }

                pedidos.add(pedido);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error al recuperar los pedidos de la base de datos."
            );

            e.printStackTrace();

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "Error al reconstruir un pedido desde la base de datos."
            );

            e.printStackTrace();
        }

        return pedidos;
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

            sentencia.setString(
                    1,
                    estado.name()
            );

            sentencia.setInt(
                    2,
                    idPedido
            );

            int filasAfectadas =
                    sentencia.executeUpdate();

            return filasAfectadas > 0;

        } catch (SQLException e) {

            System.out.println(
                    "Error al actualizar el estado del pedido."
            );

            e.printStackTrace();
        }

        return false;
    }

    private String obtenerTipoPedido(Pedido pedido) {

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