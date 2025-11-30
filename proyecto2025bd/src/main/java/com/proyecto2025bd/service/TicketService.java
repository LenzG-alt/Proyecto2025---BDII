package com.proyecto2025bd.service;

import com.proyecto2025bd.db.ConnectionFactory;
import com.proyecto2025bd.model.Technician;
import com.proyecto2025bd.model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TicketService {
    private static final Logger logger = Logger.getLogger(TicketService.class.getName());

    // -----------------------------------------------------
    // Crear un ticket
    // -----------------------------------------------------
    public int createTicket(String title, String description, int priority) {
        if (title == null || title.trim().isEmpty()) {
            logger.log(Level.WARNING, "Título no puede ser nulo o vacío.");
            return -1;
        }
        if (priority < 1 || priority > 3) {
            logger.log(Level.WARNING, "Prioridad inválida: {0}. Debe ser 1, 2 o 3.", priority);
            return -1;
        }

        String sql = "INSERT INTO tickets(titulo, descripcion, prioridad) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, priority);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                logger.log(Level.INFO, "Ticket creado con ID: {0}", id);
                return id;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al crear ticket: ", e);
        }

        return -1; // error
    }


    // -----------------------------------------------------
    // Obtener un ticket por ID
    // -----------------------------------------------------
    public Ticket getTicket(int id) {
        if (id <= 0) {
            logger.log(Level.WARNING, "ID de ticket no válido: {0}", id);
            return null;
        }

        String sql = "SELECT * FROM tickets WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Ticket(
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    rs.getString("descripcion"),
                    rs.getString("estado"),
                    rs.getInt("prioridad"),
                    rs.getTimestamp("creado_en"),
                    rs.getTimestamp("actualizado_en")
                );
            } else {
                logger.log(Level.WARNING, "No existe el ticket con ID: {0}", id);
                return null;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener ticket: ", e);
            return null;
        }
    }


    // -----------------------------------------------------
    // Cambiar estado de un ticket
    // (El trigger guardará la auditoría automáticamente)
    // -----------------------------------------------------
    public boolean updateStatus(int ticketId, String newStatus) {
        if (ticketId <= 0) {
            logger.log(Level.WARNING, "ID de ticket no válido: {0}", ticketId);
            return false;
        }
        if (newStatus == null || !isValidStatus(newStatus)) {
            logger.log(Level.WARNING, "Estado inválido: {0}", newStatus);
            return false;
        }

        String sql = "UPDATE tickets SET estado = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, ticketId);

            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                logger.log(Level.INFO, "Estado del ticket {0} actualizado a: {1}", new Object[]{ticketId, newStatus});
            }
            return success;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al actualizar estado del ticket: ", e);
            return false;
        }
    }

    private boolean isValidStatus(String status) {
        return "abierto".equals(status) || "asignado".equals(status) || "cerrado".equals(status) || "escalado".equals(status);
    }


    // -----------------------------------------------------
    // ASIGNAR TICKET (usando lógica directa en lugar de función)
    // -----------------------------------------------------
    public boolean assignTicket(int ticketId, int technicianId) {
        if (ticketId <= 0 || technicianId <= 0) {
            logger.log(Level.WARNING, "IDs inválidos: ticketId={0}, technicianId={1}", new Object[]{ticketId, technicianId});
            return false;
        }

        if (!isTechnicianActive(technicianId)) {
            logger.log(Level.WARNING, "Técnico ID {0} no está activo o no existe.", technicianId);
            return false;
        }

        String lockSql = "SELECT estado FROM tickets WHERE id = ? FOR UPDATE";
        String assignSql = """
            INSERT INTO asignaciones_tickets (id_ticket, id_tecnico)
            VALUES (?, ?)
            ON CONFLICT (id_ticket)
            DO UPDATE SET id_tecnico = ?, asignado_en = NOW()
            """;
        String updateStatusSql = "UPDATE tickets SET estado = 'asignado' WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Bloquear y verificar estado actual
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
                lockStmt.setInt(1, ticketId);
                ResultSet rs = lockStmt.executeQuery();
                if (!rs.next()) {
                    logger.log(Level.WARNING, "Ticket ID {0} no existe.", ticketId);
                    conn.rollback();
                    return false;
                }
                String currentStatus = rs.getString("estado");
                if (!"abierto".equals(currentStatus)) {
                    logger.log(Level.WARNING, "Ticket ID {0} no está en estado 'abierto' (estado actual: {1})", new Object[]{ticketId, currentStatus});
                    conn.rollback();
                    return false;
                }
            }

            // 2. Asignar
            try (PreparedStatement assignStmt = conn.prepareStatement(assignSql)) {
                assignStmt.setInt(1, ticketId);
                assignStmt.setInt(2, technicianId);
                assignStmt.setInt(3, technicianId);
                assignStmt.executeUpdate();
            }

            // 3. Actualizar estado
            try (PreparedStatement updateStmt = conn.prepareStatement(updateStatusSql)) {
                updateStmt.setInt(1, ticketId);
                updateStmt.executeUpdate();
            }

            conn.commit();
            logger.log(Level.INFO, "Ticket {0} asignado al técnico {1}", new Object[]{ticketId, technicianId});
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al asignar ticket: ", e);
            return false;
        }
    }

    private boolean isTechnicianActive(int techId) {
        String sql = "SELECT activo FROM tecnicos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, techId);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getBoolean("activo");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al verificar si técnico está activo: ", e);
            return false;
        }
    }


    // -----------------------------------------------------
    // Cerrar ticket
    // -----------------------------------------------------
    public boolean closeTicket(int id) {
        return updateStatus(id, "cerrado");
    }


    // -----------------------------------------------------
    // Listar todos los tickets
    // -----------------------------------------------------
    public List<Ticket> listTickets() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT id, titulo, estado, prioridad, creado_en, actualizado_en FROM tickets ORDER BY id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Ticket(
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    null, // description no se incluye en el SELECT para este listado
                    rs.getString("estado"),
                    rs.getInt("prioridad"),
                    rs.getTimestamp("creado_en"),
                    rs.getTimestamp("actualizado_en")
                ));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al listar tickets: ", e);
        }

        return list;
    }


    // -----------------------------------------------------
    // Listar tickets por técnico
    // -----------------------------------------------------
    public List<Ticket> listTicketsByTechnician(int techId) {
        List<Ticket> list = new ArrayList<>();
        String sql = """
            SELECT t.id, t.titulo, t.estado, t.prioridad, t.creado_en, t.actualizado_en
            FROM tickets t
            JOIN asignaciones_tickets at ON t.id = at.id_ticket
            WHERE at.id_tecnico = ?
            ORDER BY t.creado_en DESC
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, techId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Ticket(
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    null, // description
                    rs.getString("estado"),
                    rs.getInt("prioridad"),
                    rs.getTimestamp("creado_en"),
                    rs.getTimestamp("actualizado_en")
                ));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al listar tickets por técnico: ", e);
        }

        return list;
    }


    // -----------------------------------------------------
    // Obtener técnico asignado actualmente a un ticket
    // -----------------------------------------------------
    public Technician getCurrentAssignedTechnician(int ticketId) {
        String sql = """
            SELECT t.id, t.nombre, t.activo
            FROM tecnicos t
            JOIN asignaciones_tickets at ON t.id = at.id_tecnico
            WHERE at.id_ticket = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Technician(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getBoolean("activo")
                );
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al obtener técnico asignado: ", e);
        }
        return null;
    }


    // -----------------------------------------------------
    // Mostrar auditoría de un ticket
    // -----------------------------------------------------
    public void showAudit(int ticketId) {
        if (ticketId <= 0) {
            logger.log(Level.WARNING, "ID de ticket no válido para auditoría: {0}", ticketId);
            return;
        }

        String sql = "SELECT * FROM auditoria_tickets WHERE id_ticket = ? ORDER BY cambiado_en";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ticketId);

            ResultSet rs = stmt.executeQuery();

            System.out.println("=== Auditoría del ticket " + ticketId + " ===");
            while (rs.next()) {
                System.out.println(
                    rs.getTimestamp("cambiado_en") +
                    " | " + rs.getString("estado_anterior") +
                    " -> " + rs.getString("estado_nuevo")
                );
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error al mostrar auditoría del ticket: ", e);
        }
    }
}