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
        if (!card.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(card.getImageUrl(), 250, 0, true, true));
        }

        Label nameLabel = new Label("Name: " + card.getName());
        Label typeLabel = new Label("Type: " + card.getCardType());
        Label setLabel = new Label("Set: (placeholder)");
        Label priceLabel = new Label("Market Price: (placeholder)");
        Label numberLabel = new Label("Card Number: " + card.getCardNumber());

        // Back button to return to previous view
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            // Re-launch the main viewer scene
            PokemonCardViewer viewer = new PokemonCardViewer();
            viewer.start(primaryStage);
        });

        VBox layout = new VBox(10, imageView, nameLabel, typeLabel, setLabel, priceLabel, numberLabel, backButton);
        layout.setPadding(new Insets(15));

        // Optional: let layout expand if needed
        layout.setFillWidth(true);

        Scene detailScene = new Scene(layout, 600, 500);
        primaryStage.setScene(detailScene);
    }
}
