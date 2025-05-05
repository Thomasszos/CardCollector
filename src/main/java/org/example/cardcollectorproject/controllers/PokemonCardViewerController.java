package org.example.cardcollectorproject.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.example.cardcollectorproject.api.PokeAPI;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.services.CardSearching;
import org.example.cardcollectorproject.models.CardPrice;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class PokemonCardViewerController implements Initializable {

    @FXML private ComboBox<String> searchCriteriaBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> sortOptions;
    @FXML private ListView<PokemonCard> listView;

    @FXML private VBox cardDetailBox;
    @FXML private ImageView cardImageView;
    @FXML private Label nameLabel;
    @FXML private Label typeLabel;
    @FXML private Label mechanicLabel;
    @FXML private Label movesLabel;
    @FXML private Label cardNumberLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Button closeButton;
    @FXML private VBox priceHistoryBox;
    @FXML private LineChart<String, Number> priceHistoryChart;

    private final List<PokemonCard> cards = new ArrayList<>();
    private final CardSearching cardService = new CardSearching();
    private final ContextMenu autoCompletePopup = new ContextMenu();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchCriteriaBox.getItems().addAll("Name", "Type", "ID", "Set");
        searchCriteriaBox.setValue("Name");

        sortOptions.getItems().addAll("Name", "Type");
        sortOptions.setValue("Name");

        searchButton.setOnAction(e -> searchCards());
        searchField.setOnAction(e -> searchCards());
        searchField.textProperty().addListener((obs, oldText, newText) -> showAutoCompleteSuggestions(newText));

        sortOptions.setOnAction(e -> sortAndDisplayCards());
        closeButton.setOnAction(e -> hideCardDetail());

        listView.setOnMouseClicked(this::handleCardClick);

        listView.setCellFactory(lv -> new ListCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label nameLabel = new Label();
            private final HBox hBox = new HBox(10, imageView, nameLabel);

            {
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(PokemonCard card, boolean empty) {
                super.updateItem(card, empty);

                if (empty || card == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(card.getName());
                    if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
                        imageView.setImage(new Image(card.getImageUrl(), 50, 50, true, true));
                    } else {
                        imageView.setImage(null);
                    }
                    setGraphic(hBox);
                }
            }
        });
    }

    private void searchCards() {
        String criteria = searchCriteriaBox.getValue();
        String searchText = searchField.getText().trim();

        String name = "";
        String type = "";
        String id = "";
        String set = "";

        if ("Name".equals(criteria)) {
            name = searchText;
        } else if ("Type".equals(criteria)) {
            type = searchText;
        } else if ("ID".equals(criteria)) {
            id = searchText;
        } else if ("Set".equals(criteria)) {
            set = searchText;
        }

        cards.clear();
        cards.addAll(cardService.fetchCards(name, type, set, id));
        sortAndDisplayCards();
    }

    private void sortAndDisplayCards() {
        if ("Name".equals(sortOptions.getValue())) {
            cards.sort(Comparator.comparing(PokemonCard::getName));
        } else if ("Type".equals(sortOptions.getValue())) {
            cards.sort(Comparator.comparing(PokemonCard::getCardType));
        }

        listView.getItems().setAll(cards);
    }

    private void handleCardClick(MouseEvent event) {
        PokemonCard selectedCard = listView.getSelectionModel().getSelectedItem();
        if (selectedCard != null) {
            showCardDetail(selectedCard);
        }
    }

    private void showCardDetail(PokemonCard card) {
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            cardImageView.setImage(new Image(card.getImageUrl(), 200, 0, true, true));
        } else {
            cardImageView.setImage(null);
        }

        nameLabel.setText("Name: " + card.getName());
        typeLabel.setText("Type: " + card.getCardType());
        mechanicLabel.setText("Mechanic: " + card.getMechanic());
        movesLabel.setText("Moves: " + card.getMoves());
        cardNumberLabel.setText("Card #: " + card.getCardNumber());

        // Display the current price if available
        List<CardPrice> priceHistory = card.getPriceHistory();
        if (priceHistory != null && !priceHistory.isEmpty()) {
            // Get the most recent price
            CardPrice latestPrice = priceHistory.get(priceHistory.size() - 1);
            priceLabel.setText(String.format("Current Price: $%.2f", latestPrice.getPrice()));

            // Update price history chart
            updatePriceHistoryChart(priceHistory);
            priceHistoryBox.setVisible(true);
            priceHistoryBox.setManaged(true);
        } else {
            priceLabel.setText("Price: Not available");
            priceHistoryBox.setVisible(false);
            priceHistoryBox.setManaged(false);
        }

        descriptionLabel.setText("Loading description...");
        new Thread(() -> {
            String description = PokeAPI.getPokemonDescription(card.getName());
            Platform.runLater(() -> {
                descriptionLabel.setText("");
                animateDescription(description);
            });
        }).start();

        cardDetailBox.setVisible(true);
        cardDetailBox.setManaged(true);
    }

    private void animateDescription(String text) {
        final int[] index = {0};
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(30), e -> {
                    if (index[0] < text.length()) {
                        descriptionLabel.setText(descriptionLabel.getText() + text.charAt(index[0]));
                        index[0]++;
                    }
                })
        );
        timeline.setCycleCount(text.length());
        timeline.play();
    }

    private void updatePriceHistoryChart(List<CardPrice> priceHistory) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Price History");

        // Sort price history by timestamp
        priceHistory.sort(Comparator.comparing(CardPrice::getTimestamp));

        // Add data points to the series
        for (CardPrice price : priceHistory) {
            String date = price.getTimestamp().toLocalDate().toString();
            series.getData().add(new XYChart.Data<>(date, price.getPrice()));
        }

        // Clear existing data and add new series
        priceHistoryChart.getData().clear();
        priceHistoryChart.getData().add(series);
    }

    private void hideCardDetail() {
        cardDetailBox.setVisible(false);
        cardDetailBox.setManaged(false);
        priceHistoryBox.setVisible(false);
        priceHistoryBox.setManaged(false);
    }

    private void showAutoCompleteSuggestions(String query) {
        if (query.isBlank()) {
            autoCompletePopup.hide();
            return;
        }

        String criteria = searchCriteriaBox.getValue();
        List<String> suggestions = new ArrayList<>();

        for (PokemonCard card : cards) {
            String target = switch (criteria) {
                case "Name" -> card.getName();
                case "Type" -> card.getCardType();
                case "ID" -> card.getCardNumber();
                case "Set" -> "";
                default -> "";
            };

            if (target != null && target.toLowerCase().contains(query.toLowerCase()) && !suggestions.contains(target)) {
                suggestions.add(target);
            }
        }

        if (suggestions.isEmpty()) {
            autoCompletePopup.hide();
            return;
        }

        List<MenuItem> menuItems = new ArrayList<>();
        for (String suggestion : suggestions.subList(0, Math.min(suggestions.size(), 5))) {
            MenuItem item = new MenuItem(suggestion);
            item.setOnAction(e -> {
                searchField.setText(suggestion);
                searchCards();
            });
            menuItems.add(item);
        }

        autoCompletePopup.getItems().setAll(menuItems);
        autoCompletePopup.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}






