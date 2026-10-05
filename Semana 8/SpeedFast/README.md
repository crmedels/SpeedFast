# SpeedFast - Semana 8

Actividad sumativa de Desarrollo Orientado a Objetos II.

Aplicación de escritorio desarrollada en Java Swing para gestionar pedidos, repartidores y entregas. En esta semana se completaron las operaciones CRUD y su integración con MySQL mediante JDBC.

## Funcionalidades

### Repartidores

- Registrar repartidores por nombre.
- Consultar los registros en una tabla.
- Editar el nombre en una ventana de edición.
- Eliminar repartidores que no tengan entregas asociadas.

### Pedidos

- Registrar dirección, tipo y distancia en kilómetros.
- Crear pedidos con estado inicial `PENDIENTE`.
- Gestionar los tipos `COMIDA`, `ENCOMIENDA` y `EXPRESS`.
- Consultar pedidos y sus estados: `PENDIENTE`, `EN_REPARTO` y `ENTREGADO`.
- Editar dirección, tipo, distancia y estado, respetando las reglas de las entregas.
- Eliminar pedidos que no tengan entregas asociadas.
- Actualizar automáticamente el listado cada cinco segundos y mediante el botón Refrescar.

### Entregas

- Seleccionar pedidos y repartidores desde combos cargados con datos de MySQL.
- Preparar una asignación y registrar la entrega al pulsar Iniciar entrega.
- Guardar el pedido, el repartidor, la fecha y la hora de la entrega.
- Consultar las entregas en una tabla y filtrar por pedido, repartidor o ambos.
- Editar los datos de una entrega en una ventana de edición.
- Eliminar registros de entrega y finalizar entregas marcando el pedido como `ENTREGADO`.
- Refrescar tablas y combos después de las operaciones y al recuperar el foco de la ventana.

## Reglas y validaciones

- Los nombres y las direcciones son obligatorios; se validan los límites de 100 y 150 caracteres, respectivamente.
- La distancia debe ser un número entero mayor que cero al registrar o editar un pedido.
- Los pedidos antiguos cuya distancia sea `NULL` se muestran como «Sin registrar». No se interpreta una distancia desconocida como cero kilómetros.
- La fecha de edición usa el formato `AAAA-MM-DD` y un año entre 1000 y 9999. La hora usa `HH:mm:ss`; se comprueba que ambas sean válidas.
- Para iniciar una entrega, el pedido debe estar `PENDIENTE` y no tener otra entrega asociada.
- Al registrar la entrega, el pedido pasa a `EN_REPARTO`. Al finalizarla, pasa a `ENTREGADO`.
- No se permite pasar un pedido a `PENDIENTE` mientras conserve una entrega asociada, ni a `EN_REPARTO` sin una entrega.
- Al reasignar o eliminar una entrega, un pedido que estaba `EN_REPARTO` vuelve a `PENDIENTE` si queda sin entregas.
- Las correcciones o eliminaciones del historial de entregas completadas conservan el estado `ENTREGADO` del pedido anterior.
- Las claves foráneas protegen los pedidos y repartidores relacionados con entregas.
- Los resultados y errores se comunican mediante tablas y mensajes de `JOptionPane`.

## Organización del proyecto

```text
SpeedFast/
├── src/
│   ├── cl/speedfast/
│   │   ├── main/
│   │   ├── modelo/
│   │   ├── interfaces/
│   │   ├── controlador/
│   │   └── vista/
│   └── dao/
├── lib/
│   └── mysql-connector-j-26.7.0.jar
├── SpeedFast.iml
├── speedfast_db.sql
└── README.md
```

- **Modelo:** entidades y reglas del sistema, incluyendo la jerarquía de pedidos.
- **DAO:** `PedidoDAO`, `RepartidorDAO` y `EntregaDAO`, con métodos `create()`, `readAll()`, `update()` y `delete()`.
- **Conexión:** `ConexionBD`, responsable de obtener la conexión JDBC.
- **Vista:** ventanas Swing que validan entradas e invocan las operaciones DAO.
- **Controlador:** `ControladorDeEnvios`, conservado para la lógica de envíos de las semanas anteriores.

Las consultas usan `PreparedStatement` y `ResultSet`. Los recursos JDBC se cierran con `try-with-resources`; las operaciones que modifican una entrega y el estado de sus pedidos usan transacciones con `commit` y `rollback`.

## Base de datos

La base de datos se llama `speedfast_db` y contiene las tablas `pedido`, `repartidor` y `entrega`. Esta última relaciona las otras dos mediante claves foráneas.

Se conserva el esquema utilizado en la Semana 7, con nombres de tablas en singular y campos `VARCHAR` para tipo y estado. Los valores permitidos se validan desde Java. Se agregó `pedido.distancia_km` para persistir la distancia solicitada por la aplicación.

## Requisitos y ejecución

- IntelliJ IDEA y un JDK compatible. La compilación del código fue verificada con Java 17.
- MySQL instalado y en ejecución.
- MySQL Workbench o un cliente SQL para preparar la base de datos.
- El conector JDBC incluido en `lib/`.

1. Abrir la carpeta del proyecto en IntelliJ IDEA y configurar el JDK.
2. Para una instalación nueva, ejecutar `speedfast_db.sql` en MySQL Workbench. Las tablas deben estar ausentes antes de ejecutar sus sentencias `CREATE TABLE`.
3. Si la base de datos ya existe y tiene `distancia_km`, conservarla y omitir el paso anterior. No volver a ejecutar el script completo sobre las tablas existentes.
4. Configurar en `src/dao/ConexionBD.java` la URL, el usuario y la contraseña correspondientes al entorno local.
5. Comprobar que IntelliJ tenga agregado `lib/mysql-connector-j-26.7.0.jar` como biblioteca del módulo; `SpeedFast.iml` incluye esa referencia.
6. Ejecutar `cl.speedfast.main.Main`.

Si se utiliza una base de datos anterior al cambio de distancia, comprobar primero si la columna existe:

```sql
USE speedfast_db;
SHOW COLUMNS FROM pedido LIKE 'distancia_km';
```

Solo si la consulta no devuelve la columna, agregarla sin reemplazar las tablas ni sus registros:

```sql
ALTER TABLE pedido
ADD COLUMN distancia_km INT NULL
COMMENT 'Kilómetros; NULL si aún no se registró';
```

Los pedidos anteriores quedan con distancia `NULL`; su valor real puede registrarse desde la edición de pedidos.

## Comprobación del funcionamiento

1. Registrar un repartidor y un pedido con distancia positiva.
2. Editar ambos registros y comprobar los cambios en sus tablas.
3. Iniciar una entrega y verificar que el pedido pase a `EN_REPARTO`.
4. Probar los filtros, editar la entrega y finalizarla; comprobar el estado `ENTREGADO`.
5. Probar campos vacíos, distancias inválidas, fechas incorrectas y eliminaciones de registros relacionados.
6. Cerrar y volver a abrir la aplicación para comprobar que los datos permanecen almacenados.

También pueden consultarse los registros directamente en MySQL Workbench:

```sql
USE speedfast_db;
SELECT * FROM repartidor;
SELECT * FROM pedido;
SELECT * FROM entrega;
```

## Tecnologías

Java, Java Swing, JDBC, MySQL, MySQL Connector/J e IntelliJ IDEA.
