package com.proyecto2025bd;
import java.sql.*;
public class TestJdbc {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/sistema_tickets_db";
        String user = "proyecto2025";
        String pass = "proyecto2025";
        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Conectado! AutoCommit: " + c.getAutoCommit());
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT version();")) {
                    if (rs.next()) System.out.println(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("No se pudo conectar: " + e.getMessage());
        }
    }
}
