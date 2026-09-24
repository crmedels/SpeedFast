package cl.speedfast.main;

import cl.speedfast.controlador.ControladorDeEnvios;
import cl.speedfast.vista.VentanaPrincipal;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            ControladorDeEnvios controlador = new ControladorDeEnvios();

            VentanaPrincipal ventanaPrincipal =
                    new VentanaPrincipal(controlador);

            ventanaPrincipal.setVisible(true);
        });
    }
}