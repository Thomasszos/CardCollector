package org.example.cardcollectorproject.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
import org.example.cardcollectorproject.models.User;
import javafx.scene.control.Alert;

import java.util.List;

public class CardDetailView {

    private final CardSearching cardService = new CardSearching();

    public void showCardDetail(Stage detailStage, PokemonCard card) {
        // Use a separate stage for detail view
        detailStage.setTitle(card.getName() + " - Card Details");

        // Create main layout with two columns
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f5f5, #e0e0e0);");

        // Left side - Image
        VBox imageSection = new VBox(15);
        imageSection.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            Image cardImage = new Image(card.getImageUrl(), 300, 0, true, true);
            imageView.setImage(cardImage);

            // Add drop shadow effect to the card image
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
            dropShadow.setRadius(10);
            dropShadow.setOffsetX(3);
            dropShadow.setOffsetY(3);
            imageView.setEffect(dropShadow);

            // Scale up animation on hover
            imageView.setOnMouseEntered(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), imageView);
                scaleTransition.setToX(1.05);
                scaleTransition.setToY(1.05);
                scaleTransition.play();
            });

            imageView.setOnMouseExited(e -> {
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), imageView);
                scaleTransition.setToX(1.0);
                scaleTransition.setToY(1.0);
                scaleTransition.play();
            });
        }

        Label priceLabel = new Label("Market Price: Loading...");
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-background-color: rgba(76, 175, 80, 0.2); -fx-padding: 8 15; -fx-background-radius: 5;");

        VBox priceBox = new VBox(10, priceLabel);
        priceBox.setAlignment(Pos.CENTER);

        imageSection.getChildren().addAll(imageView, priceBox);
        mainLayout.setLeft(imageSection);

        // Right side - Card information
        VBox infoSection = new VBox(12);
        infoSection.setPadding(new Insets(0, 0, 0, 25));

        Label nameLabel = new Label(card.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #1976d2;");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);

        // Add details with styled labels
        int row = 0;

        Label typeHeaderLabel = new Label("Card Type:");
        typeHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        detailsGrid.add(typeHeaderLabel, 0, row);

        Label typeValueLabel = new Label(card.getCardType());
        typeValueLabel.setStyle("-fx-background-color: rgba(33, 150, 243, 0.2); -fx-padding: 3 8; -fx-background-radius: 4;");
        detailsGrid.add(typeValueLabel, 1, row++);

        Label mechanicHeaderLabel = new Label("Mechanic:");
        mechanicHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        detailsGrid.add(mechanicHeaderLabel, 0, row);

        Label mechanicValueLabel = new Label(card.getMechanic());
        mechanicValueLabel.setStyle("-fx-background-color: rgba(156, 39, 176, 0.2); -fx-padding: 3 8; -fx-background-radius: 4;");
        detailsGrid.add(mechanicValueLabel, 1, row++);

        Label setHeaderLabel = new Label("Set:");
        setHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        detailsGrid.add(setHeaderLabel, 0, row);

        Label setValueLabel = new Label("Loading...");
        setValueLabel.setStyle("-fx-background-color: rgba(255, 152, 0, 0.2); -fx-padding: 3 8; -fx-background-radius: 4;");
        detailsGrid.add(setValueLabel, 1, row++);

        Label numberHeaderLabel = new Label("Card Number:");
        numberHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        detailsGrid.add(numberHeaderLabel, 0, row);

        Label numberValueLabel = new Label(card.getCardNumber());
        numberValueLabel.setStyle("-fx-background-color: rgba(0, 150, 136, 0.2); -fx-padding: 3 8; -fx-background-radius: 4;");
        detailsGrid.add(numberValueLabel, 1, row++);

        // Description section
        TitledPane descriptionPane = new TitledPane();
        descriptionPane.setText("Pokémon Description");
        descriptionPane.setExpanded(true);

        TextArea descriptionArea = new TextArea("Loading description...");
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(100);
        descriptionArea.setStyle("-fx-control-inner-background: #f9f9f9;");

        descriptionPane.setContent(descriptionArea);

        // Buttons section
        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(20, 0, 0, 0));

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-padding: 8 20;");
        backButton.setOnAction(e -> detailStage.close());

        // Create "Remove from Collection" button
        Button removeFromCollectionButton = new Button("Remove from Collection");
        removeFromCollectionButton.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-padding: 8 20;");
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
        Button removeFromWatchlistButton = new Button("Remove from Watchlist");
        removeFromWatchlistButton.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-padding: 8 20;");
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

        Scene scene = new Scene(mainLayout, 800, 600);
        detailStage.setScene(scene);
        detailStage.show();
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