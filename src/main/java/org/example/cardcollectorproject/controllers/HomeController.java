package org.example.cardcollectorproject.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.cardcollectorproject.services.UIAnimationService;

import java.io.IOException;

public class HomeController {

    @FXML
    private Button getStartedButton;

    @FXML
    private HBox featureCardsContainer;

    @FXML
    private VBox innerContainer;

    @FXML
    private ImageView characterImage; // Reference to the left side character image

    @FXML
    private ImageView homeImage; // Reference to the right side background image

    private final UIAnimationService animationService = new UIAnimationService();

    @FXML
    private void initialize() {
        // Apply continuous hover animation to the background images
        if (characterImage != null) {
            animationService.applyHoverAnimation(characterImage);
        } else {
            System.out.println("characterImage is null");
        }

        if (homeImage != null) {
            animationService.applyHoverAnimation(homeImage);
        } else {
            System.out.println("homeImage is null");
        }

        // Apply entrance animation to the inner container
        if (innerContainer != null) {
            // Set initial state
            innerContainer.setOpacity(0);
            innerContainer.setScaleX(0.95);
            innerContainer.setScaleY(0.95);

            // Animate the inner container entrance
            animationService.fadeInWithScale(innerContainer, 0.8);
        } else {
            System.out.println("innerContainer is null");
        }

        // Use the animation service to animate feature cards
        if (featureCardsContainer != null) {
            // Animate the feature cards with a slight delay
            animationService.animateHomeScreenEntrance(featureCardsContainer);
        } else {
            System.out.println("featureCardsContainer is null");
        }
    }

    @FXML
    private void handleGetStarted(ActionEvent event) {
        try {
            // Get the parent tab pane
            Node source = (Node) event.getSource();
            Scene scene = source.getScene();
            TabPane tabPane = (TabPane) scene.lookup(".nav-tab-pane");

            // If we found the tab pane, select the search tab (index 1)
            if (tabPane != null) {
                tabPane.getSelectionModel().select(1); // Assuming search tab is at index 1
            } else {
                // Fallback if the tab pane can't be found
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/cardcollectorproject/PokemonCardViewer.fxml"));
                Parent cardView = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene cardScene = new Scene(cardView);
                stage.setScene(cardScene);
                stage.show();
            }
        } catch (Exception e) {
            System.err.println("Error navigating to search: " + e.getMessage());
            e.printStackTrace();
        }
    }
}