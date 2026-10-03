# SpeedFast - Semana 7

Actividad formativa de Desarrollo Orientado a Objetos II.

Durante esta semana se incorporó persistencia de datos al sistema SpeedFast mediante una base de datos MySQL y conexión JDBC desde Java.

## Funcionalidades implementadas

- Conexión entre Java y MySQL mediante JDBC.
- Base de datos `speedfast_db`.
- Registro de pedidos en la base de datos.
- Registro de repartidores en la base de datos.
- Registro de entregas asociando pedidos y repartidores.
- Consulta de repartidores mediante `ResultSet`.
- Consulta de pedidos almacenados en MySQL.
- Visualización de pedidos mediante `JTable`.
- Uso de `PreparedStatement` para las operaciones SQL.
- Recuperación automática de identificadores generados por MySQL.

## Clases DAO

Se incorporaron las siguientes clases para gestionar el acceso a los datos:

- `ConexionBD`
- `PedidoDAO`
- `RepartidorDAO`
- `EntregaDAO`

## Base de datos

La base de datos utilizada es:

`speedfast_db`

Contiene las tablas:

- `pedido`
- `repartidor`
- `entrega`

La tabla `entrega` relaciona los pedidos con los repartidores mediante claves foráneas.

El archivo `speedfast_db.sql` contiene el script utilizado para crear la base de datos y sus tablas.

## Tecnologías utilizadas

- Java
- Java Swing
- JDBC
- MySQL
- MySQL Connector/J
- IntelliJ IDEA

## Ejecución

Para ejecutar el proyecto es necesario:

1. Tener MySQL instalado y en ejecución.
2. Ejecutar el archivo `speedfast_db.sql`.
3. Configurar las credenciales de MySQL en `ConexionBD.java`.
4. Ejecutar `Main.java` desde IntelliJ IDEA.