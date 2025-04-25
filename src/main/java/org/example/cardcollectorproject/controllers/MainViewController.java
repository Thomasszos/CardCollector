package org.example.cardcollectorproject.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        validateUserSession();
        setupTabListener();
    }

    private void validateUserSession() {
        if (UserSession.getInstance().getCurrentUser() == null) {
            // If no user is logged in, redirect to login
            Platform.runLater(this::handleLogout);
        }
    }

    private void setupTabListener() {
       if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null && newTab.getText().equals("Log Out")) {
                    // Handle logout
                    handleLogout();
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
          
          //Scene loginScene = new Scene(loader.load());
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
//            Stage currentStage = (Stage) tabPane.getScene().getWindow();
//
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/cardcollectorproject/PokemonCardViewer.fxml"));
//            Scene searchScene = new Scene(loader.load());
//
//            currentStage.setScene(searchScene);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/cardcollectorproject/pokemoncardviewer.fxml"));
            //AnchorPane newContent = loader.load();
            VBox cardView = (VBox) loader.load();

            // Replace the content of the current tab (or a specific tab)
            tabPane.getTabs().get(1).setContent(cardView); // e.g., "Search" tab

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

