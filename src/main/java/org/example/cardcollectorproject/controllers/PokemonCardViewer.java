package org.example.cardcollectorproject.controllers;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.services.CardSearching;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PokemonCardViewer extends Application {

    private final List<PokemonCard> cards = new ArrayList<>();
    private final ListView<HBox> listView = new ListView<>();
    private final CardSearching cardService = new CardSearching();

    @Override
    public void start(Stage primaryStage) {
        TextField nameField = new TextField();
        nameField.setPromptText("Enter Pokémon name");

        TextField typeField = new TextField();
        typeField.setPromptText("Enter Pokémon type (e.g. Fire, Electric)");

        Button searchButton = new Button("Search");

        ComboBox<String> sortOptions = new ComboBox<>();
        sortOptions.getItems().addAll("Name", "Type");
        sortOptions.setValue("Name");
        sortOptions.setOnAction(e -> sortAndDisplayCards(sortOptions.getValue()));

        searchButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String type = typeField.getText().trim();
            cards.clear();
            cards.addAll(cardService.fetchCards(name, type));
            sortAndDisplayCards(sortOptions.getValue());
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(nameField, typeField, searchButton, sortOptions, listView);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Pokemon Card Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sortAndDisplayCards(String criterion) {
        if ("Name".equals(criterion)) {
            cards.sort(Comparator.comparing(PokemonCard::getName));
        } else if ("Type".equals(criterion)) {
            cards.sort(Comparator.comparing(PokemonCard::getCardType));
        }

        listView.getItems().clear();
        for (PokemonCard card : cards) {
            listView.getItems().add(createCardHBox(card));
        }
    }

    private HBox createCardHBox(PokemonCard card) {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));

        ImageView imageView = new ImageView();
        if (!card.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(card.getImageUrl(), 150, 0, true, true));
        }

        VBox infoBox = new VBox(5);
        infoBox.getChildren().addAll(
                new Label("Name: " + card.getName()),
                new Label("Type: " + card.getCardType()),
                new Label("Mechanic: " + card.getMechanic()),
                new Label("Moves: " + card.getMoves()),
                new Label("Card Number: " + card.getCardNumber())
        );

        hbox.getChildren().addAll(imageView, infoBox);

        // Make the row clickable to show detailed view
        hbox.setOnMouseClicked(e -> {
            CardDetailView detailView = new CardDetailView();
            detailView.showCardDetail((Stage) listView.getScene().getWindow(), card);
        });

        return hbox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}