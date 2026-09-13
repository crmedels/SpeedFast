# SpeedFast - Semana 5

Proyecto desarrollado para la asignatura Desarrollo Orientado a Objetos II.

## Descripción

SpeedFast es un sistema de gestión de entregas desarrollado en Java aplicando programación orientada a objetos y programación concurrente.

En esta versión correspondiente a la Semana 5, el sistema incorpora una zona de carga compartida desde la cual múltiples repartidores retiran pedidos de forma concurrente.

El acceso a este recurso compartido se controla mediante sincronización para evitar condiciones de carrera y asegurar que cada pedido sea retirado y entregado por un único repartidor.

## Objetivo de la Semana 5

Implementar mecanismos de sincronización en un entorno multihilo para controlar el acceso concurrente a recursos compartidos.

El sistema permite:

- Registrar distintos tipos de pedidos.
- Agregar pedidos a una zona de carga compartida.
- Ejecutar múltiples repartidores de forma concurrente.
- Retirar pedidos de forma segura mediante métodos sincronizados.
- Evitar que un mismo pedido sea retirado por más de un repartidor.
- Actualizar el estado de los pedidos durante el proceso de entrega.
- Registrar el historial de cada pedido.
- Esperar la finalización de todos los hilos antes de finalizar el programa.

## Estados de los pedidos

Cada pedido puede tener uno de los siguientes estados:

- `PENDIENTE`
- `EN_REPARTO`
- `ENTREGADO`

Todos los pedidos comienzan en estado `PENDIENTE`.

Cuando un repartidor retira un pedido desde la zona de carga, su estado cambia a `EN_REPARTO`.

Una vez finalizada la simulación de entrega, el pedido cambia a `ENTREGADO`.

## Concurrencia y sincronización

La clase `ZonaDeCarga` representa el recurso compartido del sistema.

Los métodos encargados de agregar y retirar pedidos utilizan `synchronized`, permitiendo que solo un hilo pueda ejecutar una operación crítica a la vez.

De esta forma, se evita que dos repartidores retiren simultáneamente el mismo pedido.

Los repartidores implementan la interfaz `Runnable` y se ejecutan mediante un `ExecutorService` con tres hilos.

## Estructura principal del proyecto

### Pedido

Clase abstracta que contiene los datos y comportamientos comunes de los pedidos.

Incluye:

- ID del pedido.
- Dirección de entrega.
- Distancia.
- Repartidor asignado.
- Estado del pedido.
- Historial de operaciones.

### EstadoPedido

Enum que representa los estados disponibles:

- `PENDIENTE`
- `EN_REPARTO`
- `ENTREGADO`

### PedidoComida

Especialización de `Pedido` para entregas de comida.

### PedidoEncomienda

Especialización de `Pedido` para entregas de encomiendas.

### PedidoExpress

Especialización de `Pedido` para entregas express.

### ZonaDeCarga

Representa el recurso compartido donde se almacenan los pedidos pendientes.

Sus métodos principales son:

- `agregarPedido()`
- `retirarPedido()`
- `getCantidadPedidosPendientes()`
- `estaVacia()`

Las operaciones de acceso a los pedidos se encuentran sincronizadas para evitar condiciones de carrera.

### Repartidor

La clase `Repartidor` implementa `Runnable`.

Cada repartidor realiza el siguiente proceso:

1. Accede a la zona de carga compartida.
2. Retira un pedido disponible.
3. Se asigna como repartidor del pedido.
4. Cambia el estado del pedido a `EN_REPARTO`.
5. Simula el proceso de entrega mediante `Thread.sleep()`.
6. Cambia el estado del pedido a `ENTREGADO`.
7. Continúa retirando pedidos hasta que no queden pedidos pendientes.

### ControladorDeEnvios

Permite registrar los pedidos y consultar el historial de operaciones de cada uno.

### Main

Es la clase principal encargada de:

- Crear los pedidos.
- Registrar los pedidos.
- Crear la zona de carga.
- Agregar los pedidos a la zona compartida.
- Crear los repartidores.
- Ejecutar los repartidores mediante `ExecutorService`.
- Esperar la finalización de todos los hilos.
- Verificar el estado final de los pedidos.
- Mostrar los historiales de ejecución.

## Simulación realizada

El sistema utiliza seis pedidos:

- Pedido #101
- Pedido #102
- Pedido #103
- Pedido #104
- Pedido #105
- Pedido #106

También utiliza tres repartidores:

- Camila
- Luis
- Daniela

Los tres repartidores trabajan simultáneamente sobre la misma zona de carga compartida.

Debido a la ejecución concurrente, el orden en que los repartidores retiran y entregan los pedidos puede cambiar entre distintas ejecuciones.

## Ejemplo de ejecución

Durante la ejecución se pueden observar mensajes como:

```text
[Repartidor - Camila] Retirando pedido #101...
[Repartidor - Luis] Retirando pedido #102...
[Repartidor - Daniela] Retirando pedido #103...

[Repartidor - Camila] Estado: EN_REPARTO
[Repartidor - Luis] Estado: EN_REPARTO
[Repartidor - Daniela] Estado: EN_REPARTO
```

Una vez completada una entrega, el estado del pedido cambia a:

```text
ENTREGADO
```

Al finalizar correctamente la simulación, el sistema muestra:

```text
Todos los pedidos han sido entregados correctamente
```

## Historial de estados

Cada pedido mantiene un historial que permite observar sus cambios durante la simulación.

Por ejemplo:

```text
Historial del pedido #101:
- Pedido creado.
- Estado inicial: PENDIENTE.
- Repartidor asignado: Camila.
- Estado actualizado: PENDIENTE -> EN_REPARTO.
- Estado actualizado: EN_REPARTO -> ENTREGADO.
```

Esto permite verificar el proceso completo realizado por cada pedido.

## Tecnologías y conceptos utilizados

- Java
- IntelliJ IDEA
- Programación orientada a objetos
- Herencia
- Polimorfismo
- Clases abstractas
- Interfaces
- `Runnable`
- `ExecutorService`
- `synchronized`
- `Thread.sleep()`
- Manejo de interrupciones
- Recursos compartidos
- Programación concurrente
- Git
- GitHub

## Resultado

La implementación permite que múltiples repartidores trabajen concurrentemente sobre una misma zona de carga sin retirar un mismo pedido más de una vez.

La sincronización de las operaciones críticas garantiza la integridad del recurso compartido y permite que todos los pedidos finalicen correctamente en estado `ENTREGADO`.