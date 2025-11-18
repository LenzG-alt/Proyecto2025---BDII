package com.proyecto2025bd.model;

public class Technician {
    private int id;
    private String name;
    private boolean active;

    public Technician(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public boolean isActive() { return active; }

    @Override
    public String toString() {
        return "Technician{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}