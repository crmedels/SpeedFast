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
 * Gestiona pedidos y valida sus estados según las entregas asociadas.
 */
public class PedidoDAO {

    public boolean create(Pedido pedido) throws SQLException {
        validarPedido(pedido);

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Los pedidos nuevos deben registrarse como PENDIENTES."
            );
        }

        String sql = """
                INSERT INTO pedido (direccion, tipo, estado)
                VALUES (?, ?, ?)
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS
             )) {

            sentencia.setString(1, pedido.getDireccionEntrega().trim());
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

                // El esquema actual no almacena la distancia.
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
        validarPedido(pedido);

        return actualizarRegistro(
                pedido.getIdPedido(),
                pedido.getEstado(),
                pedido
        );
    }

    /**
     * Bloquea el pedido mientras valida y guarda los cambios.
     * El parámetro pedido es nulo cuando solo se modifica el estado.
     */
    private boolean actualizarRegistro(
            int idPedido,
            EstadoPedido estado,
            Pedido pedido
    ) throws SQLException {

        if (idPedido <= 0 || estado == null) {
            throw new IllegalArgumentException(
                    "El ID o el estado no son válidos."
            );
        }

        String sql = pedido == null
                ? "UPDATE pedido SET estado = ? WHERE id = ?"
                : """
                  UPDATE pedido
                  SET direccion = ?, tipo = ?, estado = ?
                  WHERE id = ?
                  """;

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);

            try {
                EstadoPedido estadoAnterior =
                        bloquearPedido(conexion, idPedido);

                if (estadoAnterior == null) {
                    conexion.rollback();
                    return false;
                }

                validarEstado(
                        conexion,
                        idPedido,
                        estado,
                        estadoAnterior
                );

                try (PreparedStatement sentencia =
                             conexion.prepareStatement(sql)) {

                    if (pedido == null) {
                        sentencia.setString(1, estado.name());
                        sentencia.setInt(2, idPedido);

                    } else {
                        sentencia.setString(
                                1,
                                pedido.getDireccionEntrega().trim()
                        );
                        sentencia.setString(2, obtenerTipoPedido(pedido));
                        sentencia.setString(3, estado.name());
                        sentencia.setInt(4, idPedido);
                    }

                    sentencia.executeUpdate();
                }

                conexion.commit();
                return true;

            } catch (SQLException | IllegalArgumentException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    private EstadoPedido bloquearPedido(
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
                return resultado.next()
                        ? EstadoPedido.valueOf(
                        resultado.getString("estado")
                                .trim().toUpperCase(Locale.ROOT)
                )
                        : null;
            }
        }
    }

    private void validarEstado(
            Connection conexion,
            int idPedido,
            EstadoPedido estado,
            EstadoPedido estadoAnterior
    ) throws SQLException {

        String sql = """
                SELECT id
                FROM entrega
                WHERE id_pedido = ?
                LIMIT 1
                """;

        boolean tieneEntrega;

        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {
                tieneEntrega = resultado.next();
            }
        }

        if (estado == EstadoPedido.PENDIENTE && tieneEntrega) {
            throw new IllegalArgumentException(
                    "El pedido tiene una entrega asociada.\n"
                            + "Primero elimine la entrega asociada o reasígnela "
                            + "a otro pedido pendiente desde Gestionar entregas."
            );
        }

        if (estado == EstadoPedido.EN_REPARTO && !tieneEntrega) {
            throw new IllegalArgumentException(
                    "El pedido no tiene una entrega asociada.\n"
                            + "Asigne un repartidor e inicie la entrega "
                            + "desde Gestionar entregas."
            );
        }

        if (estado == EstadoPedido.ENTREGADO
                && !tieneEntrega
                && estadoAnterior != EstadoPedido.ENTREGADO) {

            throw new IllegalArgumentException(
                    "No puede marcar como ENTREGADO un pedido "
                            + "sin entrega asociada.\n"
                            + "Primero registre la entrega desde Gestionar entregas."
            );
        }
    }

    private void validarPedido(Pedido pedido) {
        if (pedido == null
                || pedido.getDireccionEntrega() == null
                || pedido.getDireccionEntrega().isBlank()
                || pedido.getEstado() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar una dirección y un estado válidos."
            );
        }

        if (pedido.getDireccionEntrega().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "La dirección no puede superar los 150 caracteres."
            );
        }

        obtenerTipoPedido(pedido);
    }

    public boolean delete(int idPedido) throws SQLException {
        String sql = "DELETE FROM pedido WHERE id = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);
            return sentencia.executeUpdate() > 0;
        }
    }

    /**
     * Completa el pedido de la entrega seleccionada.
     * La entrega debe existir y el pedido debe estar EN_REPARTO.
     */
    public boolean marcarEntregado(int idEntrega) throws SQLException {
        if (idEntrega <= 0) {
            throw new IllegalArgumentException(
                    "Seleccione una entrega válida."
            );
        }

        String sql = """
                UPDATE pedido p
                INNER JOIN entrega e ON e.id_pedido = p.id
                SET p.estado = 'ENTREGADO'
                WHERE e.id = ? AND p.estado = 'EN_REPARTO'
                """;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                     conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idEntrega);
            return sentencia.executeUpdate() > 0;
        }
    }

    // Compatibilidad con el código anterior.
    public boolean guardar(Pedido pedido) {
        try {
            return create(pedido);

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Pedido> listarPedidos() {
        try {
            return readAll();

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Object[]> listarTodos() {
        List<Object[]> filas = new ArrayList<>();

        for (Pedido pedido : listarPedidos()) {
            filas.add(new Object[]{
                    pedido.getIdPedido(),
                    obtenerTipoPedido(pedido),
                    pedido.getDireccionEntrega(),
                    pedido.getEstado().name()
            });
        }

        return filas;
    }

    public boolean actualizarEstado(
            int idPedido,
            EstadoPedido estado
    ) {
        try {
            return actualizarRegistro(idPedido, estado, null);

        } catch (SQLException | IllegalArgumentException e) {
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