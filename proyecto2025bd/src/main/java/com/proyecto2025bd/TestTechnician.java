package com.proyecto2025bd;

import com.proyecto2025bd.service.TechnicianService;

public class TestTechnician {
    public static void main(String[] args) {
        TechnicianService service = new TechnicianService();

        int id = service.createTechnician("Juan Pérez");
        System.out.println("Técnico creado con ID: " + id);

        System.out.println("\nListado:");
        for (String t : service.listTechnicians()) {
            System.out.println(t);
        }

        System.out.println("\nDesactivando técnico...");
        service.setActive(id, false);

        service.getTechnician(id);
    }
}

