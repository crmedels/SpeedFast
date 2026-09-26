package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/speedfast_db";

    private static final String USUARIO = "root";
    private static final String CONTRASENA = "SQL12345";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}