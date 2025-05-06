package org.example.cardcollectorproject.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.cardcollectorproject.services.UIAnimationService;
import org.example.cardcollectorproject.utils.AudioManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class SplashController implements Initializable {
    @FXML
    private StackPane splashPane;

    @FXML
    private VBox contentBox;

    @FXML
    private ProgressBar loadingBar;

    @FXML
    private ImageView logoImage;

    @FXML
    private ImageView pokeballImage;

    @FXML
    private Label loadingLabel;

    @FXML
    private Label appTitleLabel;

    @FXML
    private Label welcomeLabel;

    @FXML
    private ImageView backgroundImageSplash;

    @FXML
    private Pane scanlineContainer;

    @FXML
    private Rectangle screenOverlay;

    private Timeline typewriterTimeline;
    private List<Line> scanlines = new ArrayList<>();
    private int charIndex = 0;
    private String fullText = "";
    private final UIAnimationService animationService;

    public SplashController() {
        this.animationService = new UIAnimationService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Set initial state
        loadingBar.setProgress(0);
        contentBox.setOpacity(0);

        animationService.startBackgroundAnimation(backgroundImageSplash);

        // Make sure the screen overlay covers the entire screen
        setupScreenOverlay();

        // Create the old TV effect
        createOldTVEffect();
    }
//    @FXML
//    public void initialize() {
//        animationService.startBackgroundAnimation(backgroundImageSplash);
//    }

    private void setupScreenOverlay() {
        // Ensure the screenOverlay covers the entire area
        screenOverlay.widthProperty().bind(splashPane.widthProperty());
        screenOverlay.heightProperty().bind(splashPane.heightProperty());
        screenOverlay.setOpacity(1.0);
    }

    private void createOldTVEffect() {


        // Create the TV power-on sequence
        PauseTransition initialDelay = new PauseTransition(Duration.millis(500));
        initialDelay.setOnFinished(e -> {
            // Create the scanlines
            createScanlines();

            // Animate the scanlines passing through the screen
            animateScanlines();

            // Create the "TV turning on" effect
            Timeline tvTurnOn = new Timeline(
                    // First a white flash
                    new KeyFrame(Duration.ZERO, new KeyValue(screenOverlay.fillProperty(), Color.BLACK)),
                    new KeyFrame(Duration.millis(50), new KeyValue(screenOverlay.fillProperty(), Color.WHITE)),
                    new KeyFrame(Duration.millis(100), new KeyValue(screenOverlay.fillProperty(), Color.valueOf("#8BFC7E"))), // Game Boy green
                    new KeyFrame(Duration.millis(150), new KeyValue(screenOverlay.fillProperty(), Color.BLACK)),
                    // Then fade out to reveal content
                    new KeyFrame(Duration.millis(1000), new KeyValue(screenOverlay.opacityProperty(), 0.0))
            );

            tvTurnOn.setOnFinished(event -> {
                // After the TV turns on, show the content
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.0), contentBox);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.setOnFinished(finishEvent -> {
                    // Start the retro game intro sequence
                    playGameBoyStartupSequence();
                });

                fadeIn.play();
            });

            tvTurnOn.play();
        });

        initialDelay.play();
    }

    private void createScanlines() {
        // Will be called when the scene is laid out
        Platform.runLater(() -> {
            // Get the bounds of the root pane
            double width = splashPane.getWidth();
            double height = splashPane.getHeight();

            // Create horizontal scanlines
            int scanlineCount = 45; // Number of scanlines
            double lineSpacing = height / scanlineCount;

            for (int i = 0; i < scanlineCount; i++) {
                Line scanline = new Line(0, i * lineSpacing, width, i * lineSpacing);
                scanline.setStroke(Color.rgb(255, 255, 255, 0.15)); // Semi-transparent white
                scanline.setStrokeWidth(1);
                scanlines.add(scanline);
                scanlineContainer.getChildren().add(scanline);
            }
        });
    }

    private void animateScanlines() {
        // Will create a flickering effect for scanlines
        Timeline flickerTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> setScanlinesVisibility(true)),
                new KeyFrame(Duration.millis(50), e -> setScanlinesVisibility(false)),
                new KeyFrame(Duration.millis(80), e -> setScanlinesVisibility(true)),
                new KeyFrame(Duration.millis(200), e -> setScanlinesVisibility(false)),
                new KeyFrame(Duration.millis(230), e -> setScanlinesVisibility(true))
        );

        flickerTimeline.setCycleCount(3);
        flickerTimeline.setOnFinished(e -> {
            // After flickering, set permanent scanline effect
            setScanlinesVisibility(true);
            setScanlinesOpacity(0.15);

            // Create continuous subtle animation for scanlines
            Timeline scanlineAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(scanlineContainer.opacityProperty(), 0.15)),
                    new KeyFrame(Duration.seconds(2), new KeyValue(scanlineContainer.opacityProperty(), 0.05)),
                    new KeyFrame(Duration.seconds(4), new KeyValue(scanlineContainer.opacityProperty(), 0.15))
            );
            scanlineAnimation.setCycleCount(Timeline.INDEFINITE);
            scanlineAnimation.play();
        });

        flickerTimeline.play();
    }

    private void setScanlinesVisibility(boolean visible) {
        for (Line line : scanlines) {
            line.setVisible(visible);
        }
    }

    private void setScanlinesOpacity(double opacity) {
        for (Line line : scanlines) {
            line.setStroke(Color.rgb(255, 255, 255, opacity));
        }
    }

    private void playGameBoyStartupSequence() {
        // First animate the logo falling from top
        TranslateTransition logoFall = new TranslateTransition(Duration.seconds(1), logoImage);
        logoFall.setFromY(-200);
        logoFall.setToY(0);

        // Then pulse the logo
        ScaleTransition logoPulse = new ScaleTransition(Duration.millis(300), logoImage);
        logoPulse.setFromX(1.0);
        logoPulse.setFromY(1.0);
        logoPulse.setToX(1.1);
        logoPulse.setToY(1.1);
        logoPulse.setCycleCount(2);
        logoPulse.setAutoReverse(true);

        // Show welcome message with typewriter effect
        PauseTransition showWelcome = new PauseTransition(Duration.millis(500));
        showWelcome.setOnFinished(e -> {
            welcomeLabel.setVisible(true);
            startTypewriterEffect(welcomeLabel, "Welcome to the world of Pokémon Cards!");
        });

        // Start resource loading
        PauseTransition startLoading = new PauseTransition(Duration.seconds(3));
        startLoading.setOnFinished(e -> {
            // Show pokeball
            pokeballImage.setVisible(true);

            // Start rotation animation for pokeball
            RotateTransition pokeballSpin = new RotateTransition(Duration.seconds(2), pokeballImage);
            pokeballSpin.setByAngle(360);
            pokeballSpin.setCycleCount(Animation.INDEFINITE);
            pokeballSpin.play();

            // Start loading application resources
            loadApplicationResources()
                    .thenRun(this::showLoginScreen);

            // Animate the progress bar
            animateProgressBar();
        });

        // Create sequence of animations
        SequentialTransition sequence = new SequentialTransition(
                logoFall,
                logoPulse,
                showWelcome,
                startLoading
        );

        sequence.play();
    }

    private void startTypewriterEffect(Label label, String text) {
        fullText = text;
        charIndex = 0;
        label.setText("");

        if (typewriterTimeline != null) {
            typewriterTimeline.stop();
        }

        typewriterTimeline = new Timeline(
                new KeyFrame(Duration.millis(50), e -> {
                    if (charIndex < fullText.length()) {
                        label.setText(fullText.substring(0, ++charIndex));
                    }
                })
        );

        typewriterTimeline.setCycleCount(fullText.length());
        typewriterTimeline.play();
    }

    private CompletableFuture<Void> loadApplicationResources() {
        return CompletableFuture.runAsync(() -> {
            try {
                // Simulate resource loading with steps and retro-game loading messages
                updateProgress(0.2, "Initializing PokéDex...");
                Thread.sleep(700);

                updateProgress(0.4, "Loading card data...");
                Thread.sleep(700);

                updateProgress(0.6, "Connecting to Professor Oak...");
                Thread.sleep(700);

                updateProgress(0.8, "Preparing your journey...");
                Thread.sleep(700);

                updateProgress(1.0, "Let's get started!");
                Thread.sleep(500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void updateProgress(double progress, String message) {
        Platform.runLater(() -> {
            loadingBar.setProgress(progress);
            startTypewriterEffect(loadingLabel, message);
        });
    }

    private void animateProgressBar() {
        // Add "loading" animation to progress bar
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(loadingBar.scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(500), new KeyValue(loadingBar.scaleYProperty(), 1.1)),
                new KeyFrame(Duration.millis(1000), new KeyValue(loadingBar.scaleYProperty(), 1))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }

    private void showLoginScreen() {
        Platform.runLater(() -> {
            try {
                // Prepare the login screen
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/org/example/cardcollectorproject/login.fxml"));
                Parent loginRoot = loginLoader.load();
                Scene loginScene = new Scene(loginRoot, 1150, 680);

                // Get current stage
                Stage stage = (Stage) splashPane.getScene().getWindow();

                // CRT TV turn-off effect
                Timeline tvTurnOff = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(screenOverlay.opacityProperty(), 0.0),
                                new KeyValue(screenOverlay.fillProperty(), Color.BLACK)),
                        new KeyFrame(Duration.millis(100),
                                new KeyValue(screenOverlay.opacityProperty(), 0.5)),
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(screenOverlay.opacityProperty(), 1.0))
                );

                tvTurnOff.setOnFinished(e -> {
                    // Play the screen transition sound effect
                    AudioManager.getInstance().playSoundEffect("new-screen.wav");

                    // Set the login scene
                    stage.setScene(loginScene);

                    // Fade in the login screen
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(800), loginRoot);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });

                // Play the TV turn-off effect
                tvTurnOff.play();

            } catch (IOException e) {
                System.err.println("Error loading login screen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}