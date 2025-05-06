package org.example.cardcollectorproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.services.CardSearching;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CollectionController implements Initializable {

    // Singleton instance
    private static CollectionController instance;

    @FXML private TilePane collectionTilePane;
    @FXML private Label emptyCollectionLabel;

    private final CardSearching cardService = new CardSearching();

    public static CollectionController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set the singleton instance when this controller is initialized
        instance = this;
        loadCollection();
    }

    public void loadCollection() {
        List<PokemonCard> collectionCards = cardService.getCollection();

        collectionTilePane.getChildren().clear();

        if (collectionCards.isEmpty()) {
            emptyCollectionLabel.setVisible(true);
        } else {
            emptyCollectionLabel.setVisible(false);

            for (PokemonCard card : collectionCards) {
                collectionTilePane.getChildren().add(createCardNode(card));
            }
        }
    }

    private VBox createCardNode(PokemonCard card) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(card.getImageUrl(), 150, 0, true, true));
        }

        Label nameLabel = new Label(card.getName());
        Label setLabel = new Label(card.getSet() != null ? card.getSet() : "");

        VBox cardBox = new VBox(5, imageView, nameLabel, setLabel);
        cardBox.getStyleClass().add("card-box");

        return cardBox;
    }

    // Public method to refresh the collection when new cards are added
    public void refreshCollection() {
        loadCollection();
    }
}