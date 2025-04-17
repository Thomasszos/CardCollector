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

public class CardDetailView {

    public void showCardDetail(Stage primaryStage, PokemonCard card) {
        ImageView imageView = new ImageView();
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(card.getImageUrl(), 250, 0, true, true));
        }

        Label nameLabel = new Label("Name: " + (card.getName() != null ? card.getName() : "N/A"));
        Label typeLabel = new Label("Type: " + card.getCardType());
        Label numberLabel = new Label("Card Number: " + card.getCardNumber());
        Label setLabel = new Label("Set: N/A");
        Label priceLabel = new Label("Market Price: N/A");

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            PokemonCardViewer viewer = new PokemonCardViewer();
            viewer.start(primaryStage);
        });

        VBox layout = new VBox(10, imageView, nameLabel, typeLabel, setLabel, priceLabel, numberLabel, backButton);
        layout.setPadding(new Insets(15));
        layout.setFillWidth(true);

        Scene detailScene = new Scene(layout, 600, 500);
        primaryStage.setTitle("Card Details - " + card.getName());
        primaryStage.setScene(detailScene);
    }
}

