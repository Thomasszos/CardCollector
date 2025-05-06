package org.example.cardcollectorproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1150, 680); // dimension for background image animation to work
        stage.setTitle("Poke Market - Login");
        stage.setScene(scene);

        // lock the window size to avoid user interruption
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setFullScreen(false);

        stage.show();
    }
}
