package com.proyecto2025bd;

import com.proyecto2025bd.db.ConnectionFactory;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            System.out.println("Conexión exitosa 🎉");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

