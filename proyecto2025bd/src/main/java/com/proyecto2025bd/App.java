package com.proyecto2025bd;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        System.out.println("Iniciando conexión a la base de datos...");

        // Configuración de HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/sistema_tickects_db");
        config.setUsername("proyecto2025");
        config.setPassword("proyecto2025");
        //config.setMaximumPoolSize(10); // Número máximo de conexiones en el pool

        // Crear el pool de conexiones
        HikariDataSource dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            // Si todo va bien, se obtiene una conexión y se imprime un mensaje
            System.out.println("Conexión exitosa!");
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }

        System.out.println("Fin del programa.");
    }
}
