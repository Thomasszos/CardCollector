package org.example.cardcollectorproject.controllers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.cardcollectorproject.utils.AudioManager;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsDialogController implements Initializable {

    @FXML
    private ToggleButton toggleMusicButton;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label volumeLabel;

    @FXML
    private ToggleButton toggleSoundEffectsButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private AudioManager audioManager;
    private boolean settingsChanged = false;
    private double originalMusicVolume;
    private boolean originalMusicMuteState;
    private boolean originalSfxMuteState;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get AudioManager instance
        audioManager = AudioManager.getInstance();

        // Store original values for restore if canceled
        originalMusicVolume = audioManager.getVolume();
        originalMusicMuteState = audioManager.isMuted();
        originalSfxMuteState = audioManager.isSfxMuted();

        // Initialize with current audio settings
        toggleMusicButton.setSelected(!audioManager.isMuted());
        toggleSoundEffectsButton.setSelected(!audioManager.isSfxMuted());
        volumeSlider.setValue(audioManager.getVolume());

        // Format volume label as percentage
        volumeLabel.textProperty().bind(
                Bindings.format("%.0f%%", volumeSlider.valueProperty().multiply(100))
        );

        // Live update volume when slider moves
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            audioManager.setVolume(newVal.doubleValue());
            settingsChanged = true;
        });

        // Toggle music on/off
        toggleMusicButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            toggleMusicButton.setText(newVal ? "On" : "Off");
            audioManager.setMuted(!newVal);
            settingsChanged = true;
        });

        // Toggle sound effects on/off
        toggleSoundEffectsButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            toggleSoundEffectsButton.setText(newVal ? "On" : "Off");
            audioManager.setSfxMuted(!newVal);
            settingsChanged = true;

            // Play a test sound effect when enabling
            if (newVal) {
                audioManager.playSoundEffect("clicks.wav");
            }
        });

        // Set initial toggle button text based on state
        toggleMusicButton.setText(toggleMusicButton.isSelected() ? "On" : "Off");
        toggleSoundEffectsButton.setText(toggleSoundEffectsButton.isSelected() ? "On" : "Off");

        // Setup save button
        saveButton.setOnAction(event -> {
            // All changes are already applied in real-time
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        });

        // Setup cancel button - restore original settings
        cancelButton.setOnAction(event -> {
            if (settingsChanged) {
                audioManager.setVolume(originalMusicVolume);
                audioManager.setMuted(originalMusicMuteState);
                audioManager.setSfxMuted(originalSfxMuteState);
            }
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });

        // Add Easter Egg functionality
        setupEasterEgg();
    }




    private void setupEasterEgg() {

        TitledPane aboutPane = new TitledPane();
        aboutPane.setText("About");
        aboutPane.setExpanded(false);

        VBox aboutContent = new VBox(15);
        aboutContent.setPadding(new Insets(10));

        Label copyrightLabel = new Label("PokeMarket © 2025 - All Rights Reserved");
        Label developersLabel = new Label("Developed by The PokeMarket Team");

        // Secret button for easter egg
        Button secretButton = new Button("Special Thanks");
        secretButton.setStyle("-fx-background-color: #f0f0f0;");

        HBox secretButtonBox = new HBox(10);
        secretButtonBox.setAlignment(Pos.CENTER_LEFT);
        secretButtonBox.getChildren().add(secretButton);

        Label versionLabel = new Label("Version: 1.0.0");
        versionLabel.setStyle("-fx-font-style: italic;");

        aboutContent.getChildren().addAll(
                copyrightLabel,
                developersLabel,
                secretButtonBox,
                versionLabel
        );

        aboutPane.setContent(aboutContent);

        // Add to the parent container (we need to find the root VBox)
        VBox root = (VBox) saveButton.getParent().getParent();

        // Add before the Region filler
        int insertIndex = root.getChildren().size() - 2; // Before Region and button HBox
        root.getChildren().add(insertIndex, aboutPane);

        // Easter egg action
        secretButton.setOnAction(event -> playEasterEggVideo());
    }

    /**
     * Plays a funny video as an easter egg
     * Mutes background music during playback
     */
    private void playEasterEggVideo() {
        try {
            // Get the stage for proper parenting
            Stage parentStage = (Stage) saveButton.getScene().getWindow();

            // Create a new stage for the video
            Stage videoStage = new Stage();
            videoStage.setTitle("Special Thanks");
            videoStage.initModality(Modality.NONE); // Allows interaction with parent window
            videoStage.initOwner(parentStage);

            // Load the video file from resources
            URL videoUrl = getClass().getResource("/org/example/cardcollectorproject/videos/easter_egg.mp4");

            if (videoUrl == null) {
                // If video file doesn't exist, show a message instead
                System.err.println("Easter egg video file not found. Please add it to resources/videos folder.");

                // Show an error dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Video Not Found");
                alert.setHeaderText("Oops! The video seems to be missing.");
                alert.setContentText("Please add or check if easter_egg.mp4 is in src/main/resources/org/example/cardcollectorproject/videos/ folder.");
                alert.showAndWait();
                return;
            }

            // Create media player
            Media media = new Media(videoUrl.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);

            // Auto-size the MediaView
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(640);

            // Create a VBox to hold the MediaView
            VBox root = new VBox();
            root.setAlignment(Pos.CENTER);
            root.getChildren().add(mediaView);

            // Add controls at the bottom
            HBox controlsBox = new HBox(10);
            controlsBox.setAlignment(Pos.CENTER);
            controlsBox.setPadding(new Insets(10));

            Button playPauseButton = new Button("Pause");
            playPauseButton.setOnAction(e -> {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playPauseButton.setText("Play");
                } else {
                    mediaPlayer.play();
                    playPauseButton.setText("Pause");
                }
            });

            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> {
                mediaPlayer.stop();
                videoStage.close();
            });

            controlsBox.getChildren().addAll(playPauseButton, closeButton);
            root.getChildren().add(controlsBox);

            // Create scene and set it on the stage
            Scene scene = new Scene(root, 640, 480);
            videoStage.setScene(scene);

            // Store current audio state
            String currentTrack = audioManager.getCurrentTrack();
            boolean wasMuted = audioManager.isMuted();
            double storedVolume = audioManager.getVolume();

            // Temporarily mute the background music
            audioManager.setMuted(true);

            // Start playing when stage is shown
            videoStage.setOnShown(event -> mediaPlayer.play());

            // Restore audio settings and restart music when the window is closed
            videoStage.setOnCloseRequest(event -> {
                mediaPlayer.stop();
                mediaPlayer.dispose();

                System.out.println("Easter egg video closed. Attempting to restore audio.");
                System.out.println("Was muted: " + wasMuted);
                System.out.println("Current track: " + currentTrack);

                // Restore original audio settings
                audioManager.setMuted(wasMuted);
                audioManager.setVolume(storedVolume);

                // Always restart the background music if it was playing before
                if (!wasMuted && currentTrack != null) {
                    System.out.println("Attempting to restart background music: " + currentTrack);
                    // Force restart of background music
                    audioManager.stopBackgroundMusic(); // Ensure clean state
                    audioManager.playBackgroundMusic(currentTrack);
                }
            });

            // Show the video stage
            videoStage.show();
        } catch (Exception e) {
            System.err.println("Error playing easter egg video: " + e.getMessage());
            e.printStackTrace();
        }
    }
}