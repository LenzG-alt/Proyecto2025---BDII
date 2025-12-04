// src/main/java/com/proyecto2025bd/App.java
package com.proyecto2025bd;

import com.proyecto2025bd.service.TicketService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;  
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Iniciar el sistema de escalamiento automático
        TicketService.startAutoEscalation();

        Parent root = FXMLLoader.load(getClass().getResource("/com/proyecto2025bd/ui/MainView.fxml"));
        Scene scene = new Scene(root,900,600);

        stage.setTitle("Sistema de Tickets - Proyecto 2025BD");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Detener el sistema al cerrar la app
        TicketService.stopAutoEscalation();
    }

    public static void main(String[] args) {
        launch();
    }
}