package com.proyecto2025bd.service;

import com.proyecto2025bd.db.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicianService {

    // -----------------------------------------------------
    // Crear técnico
    // -----------------------------------------------------
    public int createTechnician(String nombre) {
        String sql = "INSERT INTO tecnicos(nombre) VALUES (?) RETURNING id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // error
    }


    // -----------------------------------------------------
    // Obtener técnico por ID
    // -----------------------------------------------------
    public void getTechnician(int id) {
        String sql = "SELECT * FROM tecnicos WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Nombre: " + rs.getString("nombre"));
                System.out.println("Activo: " + rs.getBoolean("activo"));
            } else {
                System.out.println("No existe ese técnico.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // -----------------------------------------------------
    // Listar todos los técnicos
    // -----------------------------------------------------
    public List<String> listTechnicians() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT * FROM tecnicos ORDER BY id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(
                    "ID " + rs.getInt("id") +
                    " | " + rs.getString("nombre") +
                    " | Activo: " + rs.getBoolean("activo")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // -----------------------------------------------------
    // Activar o desactivar técnico
    // -----------------------------------------------------
    public boolean setActive(int id, boolean active) {
        String sql = "UPDATE tecnicos SET activo = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, active);
            stmt.setInt(2, id);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // -----------------------------------------------------
    // Eliminar técnico 
    // -----------------------------------------------------
    public boolean deleteTechnician(int id) {
        String sql = "DELETE FROM tecnicos WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
