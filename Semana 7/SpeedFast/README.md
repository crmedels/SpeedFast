# SpeedFast - Semana 6

Proyecto desarrollado para la actividad de Semana 6 de Desarrollo Orientado a Objetos II.

## Descripción

En esta semana se implementó una interfaz gráfica en Java Swing para gestionar los pedidos de SpeedFast.

La aplicación permite:

- Registrar nuevos pedidos.
- Seleccionar el tipo de pedido: comida, encomienda o express.
- Validar los datos ingresados.
- Visualizar los pedidos registrados en una tabla.
- Asignar un repartidor.
- Iniciar la entrega de un pedido.
- Actualizar la información mostrada en pantalla.

## Tecnologías utilizadas

- Java
- Java Swing
- JFrame
- JTable
- DefaultTableModel
- JOptionPane
- IntelliJ IDEA

## Estructura del proyecto

El proyecto se encuentra organizado principalmente en los siguientes paquetes:

- `modelo`: contiene las clases relacionadas con los pedidos y sus datos.
- `vista`: contiene las ventanas gráficas del sistema.
- `controlador`: gestiona los pedidos y comunica la lógica con las vistas.
- `main`: contiene la clase principal que inicia la aplicación.
- `interfaces`: contiene las interfaces utilizadas por los pedidos.

## Ventanas principales

- `VentanaPrincipal`
- `VentanaRegistroPedido`
- `VentanaListaPedidos`
- `VentanaEntrega`

## Ejecución

La aplicación se inicia desde la clase:

`cl.speedfast.main.Main`

Al ejecutar el programa se muestra la ventana principal de SpeedFast, desde donde se puede acceder a las distintas funciones del sistema.