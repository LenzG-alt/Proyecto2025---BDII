// src/main/java/com/proyecto2025bd/ui/MainController.java
package com.proyecto2025bd.ui;

import com.proyecto2025bd.model.Technician;
import com.proyecto2025bd.model.Ticket;
import com.proyecto2025bd.model.AuditEntry; 
import com.proyecto2025bd.service.TechnicianService;
import com.proyecto2025bd.service.TicketService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MainController {

    private final TicketService ticketService = new TicketService();
    private final TechnicianService technicianService = new TechnicianService();

    // Tabs
    @FXML private TabPane mainTabPane;

    // Tickets Tab
    @FXML private TableView<Ticket> ticketsTable;
    @FXML private TableColumn<Ticket, Integer> ticketIdCol;
    @FXML private TableColumn<Ticket, String> ticketTitleCol;
    @FXML private TableColumn<Ticket, String> ticketStatusCol;
    @FXML private TableColumn<Ticket, Integer> ticketPriorityCol;
    @FXML private TableColumn<Ticket, String> ticketCreatedAtCol;
    @FXML private TableColumn<Ticket, String> ticketTechnicianCol;

    // Technicians Tab
    @FXML private TableView<Technician> techsTable;
    @FXML private TableColumn<Technician, Integer> techIdCol;
    @FXML private TableColumn<Technician, String> techNameCol;
    @FXML private TableColumn<Technician, String> techActiveCol;

    // Form fields
    @FXML private TextField ticketTitleField;
    @FXML private TextArea ticketDescField;
    @FXML private ComboBox<String> ticketPriorityCombo;
    @FXML private TextField ticketIdField;
    @FXML private TextField assignTechIdField;
    @FXML private TextField toggleTechIdField;
    @FXML private TextField techNameField;
    @FXML private TextField statusField;
    
    private final ObservableList<Ticket> ticketData = FXCollections.observableArrayList();
    private final ObservableList<Technician> techData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar tabla de tickets
        ticketIdCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        ticketTitleCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        ticketStatusCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        ticketPriorityCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getPriority()).asObject());
        ticketCreatedAtCol.setCellValueFactory(cell ->
            new SimpleStringProperty(
                Optional.ofNullable(cell.getValue().getCreatedAt())
                    .map(t -> t.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .orElse("N/A")
            )
        );
        ticketTechnicianCol.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getAssignedTechnicianName())
        );
        // Configurar tabla de técnicos
        techIdCol.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        techNameCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        techActiveCol.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().isActive() ? "Sí" : "No")
        );

        ticketsTable.setItems(ticketData);
        techsTable.setItems(techData);

        // Prioridades
        ticketPriorityCombo.setItems(FXCollections.observableArrayList("Alta (1)", "Media (2)", "Baja (3)"));
        ticketPriorityCombo.setValue("Media (2)");

        loadAllTickets();
        loadAllTechnicians();
    }

    // === TICKET ACTIONS ===

    @FXML
    private void handleCreateTicket() {
        String title = ticketTitleField.getText().trim();
        String desc = ticketDescField.getText().trim();
        String priorityText = ticketPriorityCombo.getValue();
        int priority = priorityText.contains("Alta") ? 1 : priorityText.contains("Media") ? 2 : 3;

        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Título vacío", "El título no puede estar vacío.");
            return;
        }

        int id = ticketService.createTicket(title, desc, priority);
        if (id != -1) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Ticket creado con ID: " + id);
            clearTicketForm();
            loadAllTickets();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo crear el ticket.");
        }
    }

    @FXML
    private void handleGetTicket() {
        String idText = ticketIdField.getText().trim();
        if (idText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "ID requerido", "Ingresa un ID de ticket.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            Ticket ticket = ticketService.getTicket(id);
            if (ticket != null) {
                showAlert(Alert.AlertType.INFORMATION, "Ticket #" + id,
                    "Título: " + ticket.getTitle() + "\n" +
                    "Descripción: " + ticket.getDescription() + "\n" +
                    "Estado: " + ticket.getStatus() + "\n" +
                    "Prioridad: " + ticket.getPriority() + "\n" +
                    "Creado: " + ticket.getCreatedAt());
            } else {
                showAlert(Alert.AlertType.WARNING, "No encontrado", "Ticket no existe.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID inválido", "Ingresa un número válido.");
        }
    }

    @FXML
    private void handleAssignTicket() {
        String ticketIdText = ticketIdField.getText().trim();
        String techIdText = assignTechIdField.getText().trim();

        if (ticketIdText.isEmpty() || techIdText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos requeridos", "ID de ticket y técnico son obligatorios.");
            return;
        }

        try {
            int ticketId = Integer.parseInt(ticketIdText);
            int techId = Integer.parseInt(techIdText);

            boolean success = ticketService.assignTicket(ticketId, techId);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Ticket asignado correctamente.");
                loadAllTickets();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo asignar el ticket.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "IDs inválidos", "Ambos IDs deben ser números.");
        }
    }

    @FXML
    private void handleCloseTicket() {
        String idText = ticketIdField.getText().trim();
        if (idText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "ID requerido", "Ingresa un ID de ticket.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            boolean closed = ticketService.closeTicket(id);
            if (closed) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Ticket cerrado.");
                loadAllTickets();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cerrar el ticket.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID inválido", "Ingresa un número válido.");
        }
    }

    @FXML
    private void handleShowAudit() {
        String idText = ticketIdField.getText().trim();
        if (idText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "ID requerido", "Ingresa un ID de ticket.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            List<AuditEntry> auditEntries = ticketService.getAuditEntries(id);

            if (auditEntries.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Auditoría", "No hay registros de auditoría para el ticket #" + id + ".");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("=== Auditoría del ticket #").append(id).append(" ===\n\n");
                for (AuditEntry entry : auditEntries) {
                    sb.append(entry.toString()).append("\n");
                }

                // Mostrar en un Alert con scroll si es largo
                TextArea textArea = new TextArea(sb.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setPrefSize(400, 300);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Auditoría del Ticket");
                alert.setHeaderText(null);
                alert.getDialogPane().setContent(textArea);
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID inválido", "Ingresa un número válido.");
        }
    }

    // === TECHNICIAN ACTIONS ===

    @FXML
    private void handleCreateTechnician() {
        String name = techNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nombre vacío", "El nombre no puede estar vacío.");
            return;
        }

        int id = technicianService.createTechnician(name);
        if (id != -1) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Técnico creado con ID: " + id);
            techNameField.clear();
            loadAllTechnicians();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo crear el técnico.");
        }
    }

    @FXML
    private void handleToggleTechnician() {
        String idText = toggleTechIdField.getText().trim();
        if (idText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "ID requerido", "Ingresa un ID de técnico.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            Technician tech = technicianService.getTechnician(id);
            if (tech == null) {
                showAlert(Alert.AlertType.WARNING, "No encontrado", "Técnico no existe.");
                return;
            }

            boolean newStatus = !tech.isActive();
            boolean updated = technicianService.setActive(id, newStatus);
            if (updated) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito",
                    "Técnico " + (newStatus ? "activado" : "desactivado") + ".");
                loadAllTechnicians();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el estado.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID inválido", "Ingresa un número válido.");
        }
    }

    
    @FXML
    private void handleUpdateStatus() {
        String idText = ticketIdField.getText().trim();
        String newStatus = statusField.getText().trim();

        if (idText.isEmpty() || newStatus.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos requeridos", "ID del ticket y nuevo estado son obligatorios.");
            return;
        }

        // Validar estado permitido
        if (!"abierto".equals(newStatus) && !"asignado".equals(newStatus) &&
            !"cerrado".equals(newStatus) && !"escalado".equals(newStatus)) {
            showAlert(Alert.AlertType.WARNING, "Estado inválido",
                "El estado debe ser: abierto, asignado, cerrado o escalado.");
            return;
        }

        try {
            int id = Integer.parseInt(idText);
            boolean success = ticketService.updateStatus(id, newStatus);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Estado actualizado a: " + newStatus);
                loadAllTickets(); // Refrescar tabla
                statusField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el estado.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "ID inválido", "El ID debe ser un número entero.");
        }
    }

    // === UTILS ===

    private void loadAllTickets() {
        ticketData.clear();
        // ✅ Usar el nuevo método que incluye el técnico asignado
        List<Ticket> tickets = ticketService.listTicketsWithAssignedTechnician();
        ticketData.addAll(tickets);
    }

    private void loadAllTechnicians() {
        techData.clear();
        List<Technician> techs = technicianService.listTechnicians();
        techData.addAll(techs);
    }

    private void clearTicketForm() {
        ticketTitleField.clear();
        ticketDescField.clear();
        ticketPriorityCombo.setValue("Media (2)");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}