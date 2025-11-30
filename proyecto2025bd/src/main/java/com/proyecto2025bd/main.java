package com.proyecto2025bd;

import com.proyecto2025bd.model.Technician;
import com.proyecto2025bd.model.Ticket;
import com.proyecto2025bd.service.TechnicianService;
import com.proyecto2025bd.service.TicketService;

import java.util.List;
import java.util.Scanner;

public class main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TechnicianService techService = new TechnicianService();
    private static final TicketService ticketService = new TicketService();

    public static void main(String[] args) {
        System.out.println("=== Sistema de Tickets ===");
        boolean running = true;

        while (running) {
            printMenu();
            int option = readInt("Selecciona una opción: ", 0, 10);

            switch (option) {
                case 1 -> createTicket();
                case 2 -> getTicket();
                case 3 -> listTickets();
                case 4 -> updateTicketStatus();
                case 5 -> closeTicket();
                case 6 -> assignTicket();
                case 7 -> showTicketAudit();
                case 8 -> createTechnician();
                case 9 -> listTechnicians();
                case 10 -> toggleTechnicianStatus();
                case 0 -> {
                    System.out.println("Saliendo del sistema...");
                    running = false;
                }
                default -> System.out.println("Opción no válida.");
            }
            System.out.println(); // Línea en blanco para separar
        }
    }

    private static void printMenu() {
        System.out.println("=== MENÚ PRINCIPAL ===");
        System.out.println("1. Crear ticket");
        System.out.println("2. Ver ticket por ID");
        System.out.println("3. Listar todos los tickets");
        System.out.println("4. Cambiar estado de ticket");
        System.out.println("5. Cerrar ticket");
        System.out.println("6. Asignar ticket a técnico");
        System.out.println("7. Ver auditoría de ticket");
        System.out.println("8. Crear técnico");
        System.out.println("9. Listar técnicos");
        System.out.println("10. Activar/Desactivar técnico");
        System.out.println("0. Salir");
    }

    private static void createTicket() {
        System.out.println("--- Crear Ticket ---");
        String title = readString("Título: ");
        String description = readString("Descripción: ");
        int priority = readInt("Prioridad (1=alta, 2=media, 3=baja): ", 1, 3);

        int id = ticketService.createTicket(title, description, priority);
        if (id != -1) {
            System.out.println("Ticket creado con ID: " + id);
        } else {
            System.out.println("Error al crear ticket.");
        }
    }

    private static void getTicket() {
        System.out.println("--- Ver Ticket ---");
        int id = readInt("ID del ticket: ", 1, Integer.MAX_VALUE);

        Ticket ticket = ticketService.getTicket(id);
        if (ticket != null) {
            System.out.println("ID: " + ticket.getId());
            System.out.println("Título: " + ticket.getTitle());
            System.out.println("Descripción: " + ticket.getDescription());
            System.out.println("Estado: " + ticket.getStatus());
            System.out.println("Prioridad: " + ticket.getPriority());
            System.out.println("Creado en: " + ticket.getCreatedAt());
            System.out.println("Actualizado en: " + ticket.getUpdatedAt());
        } else {
            System.out.println("No existe el ticket.");
        }
    }

    private static void listTickets() {
        System.out.println("--- Listar Tickets ---");
        List<Ticket> tickets = ticketService.listTickets();

        if (tickets.isEmpty()) {
            System.out.println("No hay tickets registrados.");
        } else {
            for (Ticket ticket : tickets) {
                System.out.println(
                    "ID " + ticket.getId() +
                    " | Título: " + ticket.getTitle() +
                    " | Estado: " + ticket.getStatus() +
                    " | Prioridad: " + ticket.getPriority() +
                    " | Creado en: " + ticket.getCreatedAt()
                );
            }
        }
    }

    private static void updateTicketStatus() {
        System.out.println("--- Cambiar Estado de Ticket ---");
        int id = readInt("ID del ticket: ", 1, Integer.MAX_VALUE);
        System.out.println("Estados válidos: abierto, asignado, cerrado, escalado");
        String newStatus = readString("Nuevo estado: ");

        boolean updated = ticketService.updateStatus(id, newStatus);
        if (updated) {
            System.out.println("Estado actualizado exitosamente.");
        } else {
            System.out.println("Error al actualizar estado.");
        }
    }

    private static void closeTicket() {
        System.out.println("--- Cerrar Ticket ---");
        int id = readInt("ID del ticket: ", 1, Integer.MAX_VALUE);

        boolean closed = ticketService.closeTicket(id);
        if (closed) {
            System.out.println("Ticket cerrado exitosamente.");
        } else {
            System.out.println("Error al cerrar ticket.");
        }
    }

    private static void assignTicket() {
        System.out.println("--- Asignar Ticket ---");
        int ticketId = readInt("ID del ticket: ", 1, Integer.MAX_VALUE);
        int techId = readInt("ID del técnico: ", 1, Integer.MAX_VALUE);

        boolean assigned = ticketService.assignTicket(ticketId, techId);
        if (assigned) {
            System.out.println("Ticket asignado exitosamente.");
        } else {
            System.out.println("Error al asignar ticket.");
        }
    }

    private static void showTicketAudit() {
        System.out.println("--- Auditoría de Ticket ---");
        int ticketId = readInt("ID del ticket: ", 1, Integer.MAX_VALUE);

        ticketService.showAudit(ticketId);
    }

    private static void createTechnician() {
        System.out.println("--- Crear Técnico ---");
        String name = readString("Nombre: ");

        int id = techService.createTechnician(name);
        if (id != -1) {
            System.out.println("Técnico creado con ID: " + id);
        } else {
            System.out.println("Error al crear técnico.");
        }
    }

    private static void listTechnicians() {
        System.out.println("--- Listar Técnicos ---");
        List<Technician> technicians = techService.listTechnicians();

        if (technicians.isEmpty()) {
            System.out.println("No hay técnicos registrados.");
        } else {
            for (Technician tech : technicians) {
                System.out.println(
                    "ID " + tech.getId() +
                    " | Nombre: " + tech.getName() +
                    " | Activo: " + (tech.isActive() ? "Sí" : "No")
                );
            }
        }
    }

    private static void toggleTechnicianStatus() {
        System.out.println("--- Activar/Desactivar Técnico ---");
        int id = readInt("ID del técnico: ", 1, Integer.MAX_VALUE);
        Technician tech = techService.getTechnician(id);

        if (tech == null) {
            System.out.println("Técnico no encontrado.");
            return;
        }

        boolean newStatus = !tech.isActive();
        boolean updated = techService.setActive(id, newStatus);
        if (updated) {
            System.out.println("Técnico " + (newStatus ? "activado" : "desactivado") + " exitosamente.");
        } else {
            System.out.println("Error al actualizar estado del técnico.");
        }
    }

    // --- Métodos auxiliares ---
    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println("Valor fuera de rango. Debe estar entre " + min + " y " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingresa un número entero.");
            }
        }
    }
}