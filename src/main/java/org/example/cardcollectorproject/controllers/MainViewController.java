package org.example.cardcollectorproject.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.example.cardcollectorproject.services.UserSession;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    @FXML
    private TabPane tabPane;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button userSettingsButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        validateUserSession();
        updateUserInfo();
        setupTabListener();
        setupButtonHandlers();
    }

    private void validateUserSession() {
        if (UserSession.getInstance().getCurrentUser() == null) {
            // If no user is logged in, redirect to login
            Platform.runLater(this::handleLogout);
        }
    }

    private void updateUserInfo() {
        if (usernameLabel != null && UserSession.getInstance().getCurrentUser() != null) {
            usernameLabel.setText(UserSession.getInstance().getCurrentUser().getUsername());
        }
    }

    private void setupButtonHandlers() {
        if (userSettingsButton != null) {
            userSettingsButton.setOnAction(event -> {
                // Show user settings dialog or navigate to settings page
                System.out.println("User settings clicked");
            });
        }
    }

    private void setupTabListener() {
        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null && newTab.getText().equals("Log Out")) {
                    // Handle logout
                    handleLogout();
                    // Return to home tab after logout initiated
                    Platform.runLater(() -> tabPane.getSelectionModel().select(0));
                }
                if(newTab != null && newTab.getText().equals("Search")) {
                    handleSearch();
                }
            });
        } else {
            System.err.println("Warning: tabPane is null during initialization");
        }
    }

    private void handleLogout() {
        try {
            // Clear the user session
            UserSession.getInstance().clearSession();
            // Get the current stage
            Stage currentStage = (Stage) tabPane.getScene().getWindow();

            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/cardcollectorproject/login.fxml"));
            Scene loginScene = new Scene(loader.load(), 1150, 680);

            // Set the scene
            currentStage.setScene(loginScene);
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/cardcollectorproject/PokemonCardViewer.fxml"));
            Parent cardView = loader.load();


            tabPane.getTabs().get(1).setContent(cardView);
        } catch (IOException e) {
            System.err.println("Error loading PokemonCardViewer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}