package com.proyecto2025bd.service;

import com.proyecto2025bd.db.ConnectionFactory;
import com.proyecto2025bd.model.Technician;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TechnicianService {
    private static final Logger logger = Logger.getLogger(TechnicianService.class.getName());

    // -----------------------------------------------------
    // Crear técnico
    // -----------------------------------------------------
    public int createTechnician(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.log(Level.WARNING, "Nombre de técnico no puede ser nulo o vacío.");
            return -1;
        }

        String sql = "INSERT INTO tecnicos(nombre) VALUES (?) RETURNING id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                logger.log(Level.INFO, "Técnico creado con ID: {0}", id);
                return id;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al crear técnico: ", e);
        }
        return -1; // error
    }


    // -----------------------------------------------------
    // Obtener técnico por ID
    // -----------------------------------------------------
    public Technician getTechnician(int id) {
        if (id <= 0) {
            logger.log(Level.WARNING, "ID de técnico no válido: {0}", id);
            return null;
        }

        String sql = "SELECT * FROM tecnicos WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Technician(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo")
                );
            } else {
                logger.log(Level.WARNING, "No existe el técnico con ID: {0}", id);
                return null;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener técnico: ", e);
            return null;
        }
    }


    // -----------------------------------------------------
    // Listar todos los técnicos
    // -----------------------------------------------------
    public List<Technician> listTechnicians() {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM tecnicos ORDER BY id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Technician(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo")
                ));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al listar técnicos: ", e);
        }

        return list;
    }


    // -----------------------------------------------------
    // Activar o desactivar técnico
    // -----------------------------------------------------
    public boolean setActive(int id, boolean active) {
        if (id <= 0) {
            logger.log(Level.WARNING, "ID de técnico no válido: {0}", id);
            return false;
        }

        String sql = "UPDATE tecnicos SET activo = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, active);
            stmt.setInt(2, id);

            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.log(Level.INFO, "Técnico ID {0} {1} exitosamente.", new Object[]{id, active ? "activado" : "desactivado"});
            }
            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar estado del técnico: ", e);
            return false;
        }
    }


    // -----------------------------------------------------
    // Eliminar técnico (física)
    // -----------------------------------------------------
    public boolean deleteTechnician(int id) {
        if (id <= 0) {
            logger.log(Level.WARNING, "ID de técnico no válido: {0}", id);
            return false;
        }

        // Verificar si tiene tickets asignados
        if (hasAssignedTickets(id)) {
            logger.log(Level.WARNING, "No se puede eliminar: el técnico ID {0} tiene tickets asignados.", id);
            return false;
        }

        String sql = "DELETE FROM tecnicos WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.log(Level.INFO, "Técnico ID {0} eliminado exitosamente.", id);
            }
            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al eliminar técnico: ", e);
            return false;
        }
    }

    // -----------------------------------------------------
    // Verificar si tiene tickets asignados
    // -----------------------------------------------------
    private boolean hasAssignedTickets(int techId) {
        String sql = "SELECT COUNT(*) FROM asignaciones_tickets WHERE id_tecnico = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, techId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al verificar tickets asignados: ", e);
        }
        return true; 
    }


}