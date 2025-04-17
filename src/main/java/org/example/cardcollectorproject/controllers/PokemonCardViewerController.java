package org.example.cardcollectorproject.controllers;

import javafx.fxml.FXML;
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

public class PokemonCardViewerController {

    @FXML private TextField nameField;
    @FXML private TextField typeField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> sortOptions;
    @FXML private ListView<HBox> listView;

    private final List<PokemonCard> cards = new ArrayList<>();
    private final CardSearching cardService = new CardSearching();

    @FXML
    public void initialize() {
        sortOptions.getItems().addAll("Name", "Type");
        sortOptions.setValue("Name");

        searchButton.setOnAction(e -> performSearch());
        sortOptions.setOnAction(e -> sortAndDisplayCards(sortOptions.getValue()));
    }

    private void performSearch() {
        String name = nameField.getText().trim();
        String type = typeField.getText().trim();

        System.out.println("Searching for: name=" + name + ", type=" + type); // Debug

        cards.clear();
        cards.addAll(cardService.fetchCards(name, type));
        System.out.println("Found cards: " + cards.size()); // Debug

        sortAndDisplayCards(sortOptions.getValue());
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
        hbox.setStyle("-fx-padding: 5;");

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

        hbox.setOnMouseClicked(e -> {
            CardDetailView detailView = new CardDetailView();
            detailView.showCardDetail((Stage) listView.getScene().getWindow(), card);
        });

        return hbox;
    }
}


