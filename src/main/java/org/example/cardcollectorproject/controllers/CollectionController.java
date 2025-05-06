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
import org.example.cardcollectorproject.services.UserSession;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CollectionController implements Initializable {

    private static CollectionController instance;

    @FXML private TilePane collectionTilePane;
    @FXML private Label emptyCollectionLabel;
    @FXML private Label userLabel;

    private final CardSearching cardService = new CardSearching();

    public static CollectionController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        // Show username if logged in
        if (UserSession.getInstance().isLoggedIn()) {
            String username = UserSession.getInstance().getCurrentUser().getUsername();
            userLabel.setText(username + "'s Collection");
        } else {
            userLabel.setText("Card Collection");
        }

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

    public void refreshCollection() {
        loadCollection();
    }
}