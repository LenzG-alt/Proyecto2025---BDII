package com.proyecto2025bd.service;

import com.proyecto2025bd.db.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketService {

    // -----------------------------------------------------
    // Crear un ticket
    // -----------------------------------------------------
    public int createTicket(String title, String description, int priority) {
        String sql = "INSERT INTO tickets(title, description, priority) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, priority);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);  // ID generado
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // error
    }


    // -----------------------------------------------------
    // Obtener un ticket por ID
    // -----------------------------------------------------
    public void getTicket(int id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Título: " + rs.getString("title"));
                System.out.println("Descripción: " + rs.getString("description"));
                System.out.println("Estado: " + rs.getString("status"));
                System.out.println("Prioridad: " + rs.getInt("priority"));
                System.out.println("Creado el: " + rs.getTimestamp("created_at"));
            } else {
                System.out.println("No existe el ticket");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // -----------------------------------------------------
    // Cambiar estado de un ticket
    // (El trigger guardará la auditoría automáticamente)
    // -----------------------------------------------------
    public boolean updateStatus(int ticketId, String newStatus) {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, ticketId);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // -----------------------------------------------------
    // ASIGNAR TICKET (esta es la parte importante)
    // Usa transacción y llama a la función assign_ticket()
    // -----------------------------------------------------
    public boolean assignTicket(int ticketId, int technicianId) {
        String sql = "SELECT assign_ticket(?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);  // iniciar transacción

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, ticketId);
                stmt.setInt(2, technicianId);

                ResultSet rs = stmt.executeQuery();
                rs.next();
                boolean ok = rs.getBoolean(1);

                if (ok) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // -----------------------------------------------------
    // Cerrar ticket
    // -----------------------------------------------------
    public boolean closeTicket(int id) {
        return updateStatus(id, "closed");
    }


    // -----------------------------------------------------
    // Listar todos los tickets
    // -----------------------------------------------------
    public List<String> listTickets() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT id, title, status, priority FROM tickets ORDER BY id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String line =
                        "ID " + rs.getInt("id") +
                        " | " + rs.getString("title") +
                        " | Estado: " + rs.getString("status") +
                        " | Prioridad: " + rs.getInt("priority");

                list.add(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    // -----------------------------------------------------
    // Mostrar auditoría de un ticket
    // -----------------------------------------------------
    public void showAudit(int ticketId) {
        String sql = "SELECT * FROM ticket_audit_log WHERE ticket_id = ? ORDER BY changed_at";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ticketId);

            ResultSet rs = stmt.executeQuery();

            System.out.println("=== Auditoría del ticket " + ticketId + " ===");
            while (rs.next()) {
                System.out.println(
                    rs.getTimestamp("changed_at") +
                    " | " + rs.getString("old_status") +
                    " → " + rs.getString("new_status")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
