package com.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create root container
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Add title label
        Label titleLabel = new Label("E-Commerce Application");
        titleLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        // Add a welcome button
        Button welcomeButton = new Button("Welcome");
        welcomeButton.setStyle("-fx-font-size: 14; -fx-padding: 10;");
        welcomeButton.setOnAction(e -> handleWelcomeAction());

        // Add components to root
        root.getChildren().addAll(titleLabel, welcomeButton);

        // Create scene
        Scene scene = new Scene(root, 600, 400);

        // Set stage properties
        primaryStage.setTitle("E-Commerce App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleWelcomeAction() {
        System.out.println("Welcome button clicked!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
