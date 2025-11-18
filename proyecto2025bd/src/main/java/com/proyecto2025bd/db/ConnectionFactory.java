package com.proyecto2025bd.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String URL = "jdbc:postgresql://localhost:5432/sistema_tickets_db";
    private static final String USER = "proyecto2025";
    private static final String PASSWORD = "proyecto2025";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}