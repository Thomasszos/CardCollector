package org.example.cardcollectorproject.controllers;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.cardcollectorproject.models.PokemonCard;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import org.example.cardcollectorproject.api.TCGio;
import javafx.scene.control.Alert;

public class CardDetailView {

    public void showCardDetail(Stage primaryStage, PokemonCard card) {
        ImageView imageView = new ImageView();
        if (!card.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(card.getImageUrl(), 250, 0, true, true));
        }

        Label nameLabel = new Label("Name: " + card.getName());
        Label typeLabel = new Label("Type: " + card.getCardType());
        Label setLabel = new Label("Set: fetching...");
        Label priceLabel = new Label("Market Price: fetching...");
        Label numberLabel = new Label("Card Number: " + card.getCardNumber());

        TCGio.fetchCardSet(card.getCardNumber())
                .thenAccept(setName -> Platform.runLater(() -> {
                    setLabel.setText("Set: " + setName);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> setLabel.setText("Set: unavailable"));
                    return null;
                });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            try {
                new PokemonCardViewer().start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Navigation Error");
                    alert.setHeaderText("Unable to return to the main view");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        });

        fetchAndDisplayPrice(card, priceLabel);
        Timeline refresher = new Timeline(
                new KeyFrame(Duration.ZERO,        ev -> fetchAndDisplayPrice(card, priceLabel)),
                new KeyFrame(Duration.minutes(5),  ev -> fetchAndDisplayPrice(card, priceLabel))
        );
        refresher.setCycleCount(Timeline.INDEFINITE);
        refresher.play();

        VBox layout = new VBox(10, imageView, nameLabel, typeLabel, setLabel, priceLabel, numberLabel, backButton);
        layout.setPadding(new Insets(15));

        layout.setFillWidth(true);

        Scene detailScene = new Scene(layout, 800, 600);
        primaryStage.setScene(detailScene);
    }
    private void fetchAndDisplayPrice(PokemonCard card, Label priceLabel) {
        TCGio.fetchCardPrice(card.getCardNumber())
                .thenAccept(price -> {
                    Platform.runLater(() -> {
                        if (price > 0) {
                            priceLabel.setText(String.format("Market Price: $%.2f", price));
                        } else {
                            priceLabel.setText("Market Price: unavailable");
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> priceLabel.setText("Market Price: error"));
                    return null;
                });
    }
}