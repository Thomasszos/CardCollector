package org.example.cardcollectorproject.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.example.cardcollectorproject.models.PokemonCard;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.util.Duration;
import org.example.cardcollectorproject.api.TCGio;
import org.example.cardcollectorproject.api.PokeAPI;
import org.example.cardcollectorproject.services.CardSearching;
import org.example.cardcollectorproject.services.UIAnimationService;

import java.util.List;

public class CardDetailView {

    private final CardSearching cardService = new CardSearching();
    private final UIAnimationService animationService = new UIAnimationService();

    public void showCardDetail(Stage detailStage, PokemonCard card) {
        // Use a separate stage for detail view
        detailStage.setTitle(card.getName() + " - Card Details");

        // Create the main stack pane to allow background layering
        StackPane root = new StackPane();

        // Add a blur background with the card image
        ImageView backgroundImageView = new ImageView();
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            Image cardImage = new Image(card.getImageUrl(), 1200, 0, true, true);
            backgroundImageView.setImage(cardImage);
            backgroundImageView.setOpacity(0.15); // Very transparent
            backgroundImageView.setEffect(new GaussianBlur(20)); // Strong blur effect

            // Start animation on the background
            animationService.startBackgroundAnimation(backgroundImageView);
        }

        // Create a semi-transparent color overlay
        Rectangle overlay = new Rectangle();
        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());

        // Define gradient background based on card type color
        String baseColor = getColorForType(card.getCardType());

        // Create a linear gradient with the base color
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(baseColor + "CC")), // Semi-transparent start color
                new Stop(1, Color.web("#212121CC"))  // Semi-transparent dark end
        );
        overlay.setFill(gradient);

        // Create main layout with two columns
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: transparent;");

        // Left side - Image
        VBox imageSection = new VBox(15);
        imageSection.setAlignment(Pos.CENTER);

        // Create card image view - keeping this as a final reference since we'll use it in lambdas
        final ImageView originalImageView = new ImageView();
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            Image cardImage = new Image(card.getImageUrl(), 300, 0, true, true);
            originalImageView.setImage(cardImage);

            // Add 3D-like effects to the card image
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.7));
            dropShadow.setRadius(15);
            dropShadow.setOffsetX(5);
            dropShadow.setOffsetY(5);

            // Add reflection effect for a glossy look
            Reflection reflection = new Reflection();
            reflection.setFraction(0.25);
            reflection.setTopOpacity(0.5);
            reflection.setBottomOpacity(0);

            // Combine effects - create a separate group and snapshot it
            Group effectGroup = new Group(originalImageView);
            effectGroup.setEffect(dropShadow);

            // Create a new image view with the effects applied
            final ImageView displayImageView = new ImageView(takeSnapshot(effectGroup));
            displayImageView.setEffect(reflection);

            // Scale up animation on hover - use the displayImageView consistently
            displayImageView.setOnMouseEntered(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), displayImageView);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            });

            displayImageView.setOnMouseExited(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), displayImageView);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });

            // Add the final image view to the section
            imageSection.getChildren().add(displayImageView);
        }

        // Create stylish price label
        Label priceLabel = new Label("Market Price: Loading...");
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setTextFill(Color.WHITE);
        priceLabel.setStyle("-fx-background-color: rgba(76, 175, 80, 0.7); -fx-padding: 10 20; -fx-background-radius: 30;");

        VBox priceBox = new VBox(10, priceLabel);
        priceBox.setAlignment(Pos.CENTER);

        imageSection.getChildren().add(priceBox);
        mainLayout.setLeft(imageSection);

        // Right side - Card information
        VBox infoSection = new VBox(12);
        infoSection.setPadding(new Insets(0, 0, 0, 25));
        infoSection.setStyle("-fx-background-color: rgba(33, 33, 33, 0.6); -fx-background-radius: 15; -fx-padding: 20;");

        Label nameLabel = new Label(card.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setEffect(new DropShadow(5, Color.BLACK));

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + baseColor + ";");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(15);
        detailsGrid.setVgap(15);

        // Add details with styled labels
        int row = 0;

        // Style all headers and values
        Label typeHeaderLabel = createHeaderLabel("Card Type:");
        Label typeValueLabel = createValueLabel(card.getCardType(), baseColor);
        detailsGrid.add(typeHeaderLabel, 0, row);
        detailsGrid.add(typeValueLabel, 1, row++);

        Label mechanicHeaderLabel = createHeaderLabel("Mechanic:");
        Label mechanicValueLabel = createValueLabel(card.getMechanic(), "#9C27B0");
        detailsGrid.add(mechanicHeaderLabel, 0, row);
        detailsGrid.add(mechanicValueLabel, 1, row++);

        Label setHeaderLabel = createHeaderLabel("Set:");
        // Create final reference for the label that will be updated in lambda
        final Label setValueLabel = createValueLabel("Loading...", "#FF9800");
        detailsGrid.add(setHeaderLabel, 0, row);
        detailsGrid.add(setValueLabel, 1, row++);

        Label numberHeaderLabel = createHeaderLabel("Card Number:");
        Label numberValueLabel = createValueLabel(card.getCardNumber(), "#009688");
        detailsGrid.add(numberHeaderLabel, 0, row);
        detailsGrid.add(numberValueLabel, 1, row++);

        // Description section
        TitledPane descriptionPane = new TitledPane();
        descriptionPane.setText("Pokémon Description");
        descriptionPane.setExpanded(true);
        descriptionPane.setTextFill(Color.WHITE);
        descriptionPane.setStyle("-fx-background-color: rgba(33, 33, 33, 0.4); -fx-text-fill: white;");

        // Create final reference for the text area that will be updated in lambda
        final TextArea descriptionArea = new TextArea("Loading description...");
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(100);
        descriptionArea.setStyle("-fx-control-inner-background: rgba(33, 33, 33, 0.6); -fx-text-fill: white;");

        descriptionPane.setContent(descriptionArea);

        // Buttons section with modern design
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));

        Button backButton = createStyledButton("Back", "#2196f3");
        backButton.setOnAction(e -> detailStage.close());

        // Create "Remove from Collection" button
        Button removeFromCollectionButton = createStyledButton("Remove from Collection", "#e53935");
        removeFromCollectionButton.setOnAction(e -> {
            cardService.removeFromCollection(card.getCardNumber());
            showSuccessAlert("Collection", card.getName(), true);

            // Refresh the collection view if available
            CollectionController collectionController = CollectionController.getInstance();
            if (collectionController != null) {
                collectionController.refreshCollection();
            }

            detailStage.close();
        });

        // Create "Remove from Watchlist" button
        Button removeFromWatchlistButton = createStyledButton("Remove from Watchlist", "#e53935");
        removeFromWatchlistButton.setOnAction(e -> {
            cardService.removeFromWatchlist(card.getCardNumber());
            showSuccessAlert("Watchlist", card.getName(), true);
            detailStage.close();
        });

        buttonBar.getChildren().add(backButton);

        // We're in collection view, so add the remove buttons
        buttonBar.getChildren().add(removeFromCollectionButton);

        // Check if card is also in watchlist
        if (isCardInWatchlist(card)) {
            buttonBar.getChildren().add(removeFromWatchlistButton);
        }

        infoSection.getChildren().addAll(nameLabel, separator, detailsGrid, descriptionPane, buttonBar);
        mainLayout.setCenter(infoSection);

        // Create final layered structure
        root.getChildren().addAll(backgroundImageView, overlay, mainLayout);

        // Add fade-in animation for the entire UI
        root.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Load data asynchronously
        fetchAndDisplayPrice(card, priceLabel);

        // Set up price refresh timer
        Timeline refresher = new Timeline(
                new KeyFrame(Duration.ZERO, ev -> fetchAndDisplayPrice(card, priceLabel)),
                new KeyFrame(Duration.minutes(5), ev -> fetchAndDisplayPrice(card, priceLabel))
        );
        refresher.setCycleCount(Timeline.INDEFINITE);
        refresher.play();

        // Fetch set information
        TCGio.fetchCardSet(card.getCardNumber())
                .thenAccept(setName -> Platform.runLater(() -> {
                    setValueLabel.setText(setName);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> setValueLabel.setText("Unavailable"));
                    return null;
                });

        // Fetch Pokémon description
        new Thread(() -> {
            String description = PokeAPI.getPokemonDescription(card.getName());
            Platform.runLater(() -> {
                if (description != null && !description.isEmpty()) {
                    // Animate the description text
                    descriptionArea.setText("");
                    animateDescription(description, descriptionArea);
                } else {
                    descriptionArea.setText("No description available for this Pokémon.");
                }
            });
        }).start();

        Scene scene = new Scene(root, 800, 600);
        detailStage.setScene(scene);
        detailStage.show();
    }

    // Helper method to create a styled header label
    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setTextFill(Color.WHITE);
        label.setEffect(new DropShadow(2, Color.BLACK));
        return label;
    }

    // Helper method to create a styled value label
    private Label createValueLabel(String text, String colorHex) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + colorHex + "55; -fx-padding: 5 10; -fx-background-radius: 4;");
        label.setTextFill(Color.WHITE);
        return label;
    }

    // Helper method to create styled buttons
    private Button createStyledButton(String text, String colorHex) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 30;");

        // Store the base style as a final variable for use in lambdas
        final String baseStyle = "-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 30;";
        final String hoverStyle = "-fx-background-color: " + colorHex + "DD; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 30;";

        // Add hover effect with final variables
        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
            scaleTransition.setToX(1.05);
            scaleTransition.setToY(1.05);
            scaleTransition.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(baseStyle);
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });

        return button;
    }

    // Helper method to create image snapshot for effects
    private Image takeSnapshot(Node node) {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return node.snapshot(params, null);
    }

    // Get color based on Pokémon type
    private String getColorForType(String type) {
        if (type == null || type.isEmpty()) {
            return "#607D8B"; // Default color
        }

        switch (type.toLowerCase()) {
            case "grass": return "#4CAF50";
            case "fire": return "#F44336";
            case "water": return "#2196F3";
            case "electric": return "#FFEB3B";
            case "psychic": return "#9C27B0";
            case "fighting": return "#FF5722";
            case "darkness": return "#424242";
            case "metal": return "#9E9E9E";
            case "dragon": return "#673AB7";
            case "fairy": return "#E91E63";
            case "colorless": return "#BDBDBD";
            default: return "#607D8B"; // Default for unknown types
        }
    }

    private boolean isCardInWatchlist(PokemonCard card) {
        List<PokemonCard> watchlist = cardService.getWatchlist();
        return watchlist.stream()
                .anyMatch(c -> c.getCardNumber().equals(card.getCardNumber()));
    }

    private void showSuccessAlert(String destination, String cardName, boolean isRemoval) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (isRemoval) {
            alert.setTitle("Card Removed");
            alert.setContentText(cardName + " has been removed from your " + destination + "!");
        } else {
            alert.setTitle("Card Added");
            alert.setContentText(cardName + " has been added to your " + destination + "!");
        }
        alert.setHeaderText(null);
        alert.show();

        // Auto-close the alert after 2 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> alert.close()));
        timeline.play();
    }

    private void animateDescription(String text, TextArea area) {
        final int[] index = {0};
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(30), e -> {
                    if (index[0] < text.length()) {
                        area.setText(area.getText() + text.charAt(index[0]));
                        index[0]++;
                    }
                })
        );
        timeline.setCycleCount(text.length());
        timeline.play();
    }

    private void fetchAndDisplayPrice(PokemonCard card, Label priceLabel) {
        TCGio.fetchCardPrice(card.getCardNumber())
                .thenAccept(price -> {
                    Platform.runLater(() -> {
                        if (price > 0) {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), priceLabel);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.7);

                            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), priceLabel);
                            fadeIn.setFromValue(0.7);
                            fadeIn.setToValue(1.0);

                            fadeOut.setOnFinished(e -> {
                                priceLabel.setText(String.format("Market Price: $%.2f", price));
                                fadeIn.play();
                            });

                            fadeOut.play();
                        } else {
                            priceLabel.setText("Market Price: Unavailable");
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> priceLabel.setText("Market Price: Error"));
                    return null;
                });
    }
}