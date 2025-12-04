package com.proyecto2025bd.model;

import java.sql.Timestamp;

public class AuditEntry {
    private final int ticketId;
    private final String previousStatus;
    private final String newStatus;
    private final Timestamp changedAt;

    public AuditEntry(int ticketId, String previousStatus, String newStatus, Timestamp changedAt) {
        this.ticketId = ticketId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    // Getters
    public int getTicketId() { return ticketId; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public Timestamp getChangedAt() { return changedAt; }

    @Override
    public String toString() {
        return String.format("%s | %s → %s",
            changedAt.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            previousStatus,
            newStatus
        );
    }
}