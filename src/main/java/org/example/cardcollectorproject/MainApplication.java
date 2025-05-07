
package org.example.cardcollectorproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.cardcollectorproject.utils.AudioManager;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        try {
            // Start background music
            AudioManager.getInstance().playBackgroundMusic("background_music.mp3");

            // Load the splash screen
            FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("splashscreen.fxml"));
            Parent splashRoot = splashLoader.load();

            // For debugging
            if (splashRoot == null) {
                System.err.println("Failed to load splashscreen.fxml");
                // Fallback to login screen
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent loginRoot = loginLoader.load();
                Scene loginScene = new Scene(loginRoot, 1150, 680);
                stage.setScene(loginScene);
            } else {
                Scene splashScene = new Scene(splashRoot, 900, 500);
                stage.setScene(splashScene);
            }

            // Configure the stage
            stage.setTitle("Card Collector");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();


            // Stop music when application closes
            stage.setOnCloseRequest(event -> {
                AudioManager.getInstance().stopBackgroundMusic();
            });

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();

            // Fallback to direct login if something goes wrong
            try {
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("login.fxml"));
                Scene loginScene = new Scene(loginLoader.load(), 1150, 680);
                stage.setScene(loginScene);
                stage.show();
            } catch (IOException ex) {
                System.err.println("Fatal error: Could not load login screen: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}