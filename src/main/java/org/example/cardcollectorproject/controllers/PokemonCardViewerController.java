package org.example.cardcollectorproject.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import org.example.cardcollectorproject.api.TCGio;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.services.CardSearching;
import org.example.cardcollectorproject.services.PriceTrackingService;
import org.example.cardcollectorproject.services.CardPriceRepository;
import org.example.cardcollectorproject.models.CardPrice;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import org.example.cardcollectorproject.utils.AudioManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import org.example.cardcollectorproject.services.CosmosCardPriceRepository;
import org.example.cardcollectorproject.services.CosmosDbService;

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
    @FXML private Button closeButton;
    @FXML private Label setLabel;
    @FXML private Label priceLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Button addToCollectionButton;
    @FXML private Button addToWatchlistButton;
    @FXML private LineChart<String, Number> priceHistoryChart;
    @FXML private VBox priceHistoryContainer;

    private final List<PokemonCard> cards = new ArrayList<>();
    private final CardSearching cardService = new CardSearching();
    private final ContextMenu autoCompletePopup = new ContextMenu();

    private final PriceTrackingService priceTrackingService = new PriceTrackingService(
        new CosmosCardPriceRepository(new CosmosDbService())
    );

    private final AudioManager audioManager = AudioManager.getInstance();


    private int currentPage = 1;
    private final int pageSize = 10;
    private PokemonCard selectedCard;

    private CollectionController collectionController;
    private WatchlistController watchlistController;

    // Then add a setter method so the main application can set the reference
    public void setCollectionController(CollectionController collectionController) {
        this.collectionController = collectionController;
    }

    public void setWatchlistController(WatchlistController watchlistController) {
        this.watchlistController = watchlistController;
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchCriteriaBox.getItems().addAll("Name", "Type", "ID", "Set");
        searchCriteriaBox.setValue("Name");

        sortOptions.getItems().addAll("Name", "Type");
        sortOptions.setValue("Name");



        // Add click sound to search field Enter key action
        searchField.setOnAction(e -> {

            searchCards();
        });

        searchButton.setOnAction(e -> {
            playButtonClickSound();
            currentPage = 1;
            searchCards();
        });
        searchField.setOnAction(e -> {
          playButtonClickSound();
            currentPage = 1;
            searchCards();
        });
        searchField.textProperty().addListener((obs, oldText, newText) -> showAutoCompleteSuggestions(newText));

        // Add click sound to sort options
        sortOptions.setOnAction(e -> {
            playButtonClickSound();
            sortAndDisplayCards();
        });

        // Add click sound to close button
        closeButton.setOnAction(e -> {
            playButtonClickSound();
            hideCardDetail();
        });

        // Add click sound to searchCriteriaBox
        searchCriteriaBox.setOnAction(e -> playButtonClickSound());

        prevPageButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                searchCards();
            }
        });

        nextPageButton.setOnAction(e -> {
            currentPage++;
            searchCards();
        });

        addToCollectionButton.setOnAction(e -> {
            if (selectedCard != null) {
                List<PokemonCard> collection = cardService.getCollection();
                boolean isInCollection = collection.stream()
                        .anyMatch(c -> c.getCardNumber().equals(selectedCard.getCardNumber()));

                if (isInCollection) {
                    // Remove from collection
                    cardService.removeFromCollection(selectedCard.getCardNumber());
                    addToCollectionButton.setText("Add to Collection");
                    addToCollectionButton.setStyle("");
                    showSuccessAlert("Collection", selectedCard.getName(), true);
                } else {
                    // Add to collection
                    cardService.addToCollection(selectedCard);
                    addToCollectionButton.setText("Remove from Collection");
                    addToCollectionButton.setStyle("-fx-background-color: #ff6b6b;");
                    showSuccessAlert("Collection", selectedCard.getName(), false);
                }

                // Refresh the collection view if available
                CollectionController collectionController = CollectionController.getInstance();
                if (collectionController != null) {
                    collectionController.refreshCollection();
                }
            }
        });



        addToWatchlistButton.setOnAction(e -> {
            if (selectedCard != null) {
                List<PokemonCard> watchlist = cardService.getWatchlist();
                boolean isInWatchlist = watchlist.stream()
                        .anyMatch(c -> c.getCardNumber().equals(selectedCard.getCardNumber()));

                if (isInWatchlist) {
                    // Remove from watchlist
                    cardService.removeFromWatchlist(selectedCard.getCardNumber());
                    addToWatchlistButton.setText("Add to Watchlist");
                    addToWatchlistButton.setStyle("");
                    showSuccessAlert("Watchlist", selectedCard.getName(), true);
                } else {
                    // Add to watchlist
                    cardService.addToWatchlist(selectedCard);
                    addToWatchlistButton.setText("Remove from Watchlist");
                    addToWatchlistButton.setStyle("-fx-background-color: #ff6b6b;");
                    showSuccessAlert("Watchlist", selectedCard.getName(), false);
                }

                // Refresh the watchlist view if available
                WatchlistController watchlistController = WatchlistController.getInstance();
                if (watchlistController != null) {
                    watchlistController.refreshWatchlist();
                }
            }
        });

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
    /**
     * Play the button click sound effect
     */
    private void playButtonClickSound() {
        audioManager.playSoundEffect("clicks.wav");
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
        cards.addAll(cardService.fetchCards(name, type, set, id, currentPage, pageSize));
        sortAndDisplayCards();
    }

    private void sortAndDisplayCards() {
        if ("Name".equals(sortOptions.getValue())) {
            cards.sort(Comparator.comparing(PokemonCard::getName));
        } else if ("Type".equals(sortOptions.getValue())) {
            cards.sort(Comparator.comparing(PokemonCard::getCardType));
        }

        listView.getItems().setAll(cards);
        pageInfoLabel.setText("Page: " + currentPage);
    }

    private void handleCardClick(MouseEvent event) {
        selectedCard = listView.getSelectionModel().getSelectedItem();
        if (selectedCard != null) {
            playButtonClickSound();
            showCardDetail(selectedCard);
        }
    }

    private void showCardDetail(PokemonCard card) {
        scrollPane.setVvalue(0);
        double availableWidth = scrollPane.getWidth() - 30;
        movesLabel.setMaxWidth(availableWidth);
        descriptionLabel.setMaxWidth(availableWidth);

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
        setLabel.setText("Set: " + (card.getSet() != null ? card.getSet() : "Loading..."));
        priceLabel.setText("Market Price: Loading...");

        new Thread(() -> {
            double price = TCGio.fetchCardPrice(card.getCardNumber()).join();
            Platform.runLater(() -> priceLabel.setText(String.format("Price: $%.2f", price)));
            priceTrackingService.savePriceForCard(card, price);
            List<CardPrice> history = priceTrackingService.getPriceHistory(card);
            Platform.runLater(() -> updatePriceHistoryChart(history));
        }).start();

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
        priceHistoryContainer.setVisible(true);
        priceHistoryContainer.setManaged(true);

        // Check if the card is already in the collection or watchlist
        List<PokemonCard> collection = cardService.getCollection();
        boolean isInCollection = collection.stream()
                .anyMatch(c -> c.getCardNumber().equals(card.getCardNumber()));

        if (isInCollection) {
            addToCollectionButton.setText("Remove from Collection");
            addToCollectionButton.setStyle("-fx-background-color: #ff6b6b;");
        } else {
            addToCollectionButton.setText("Add to Collection");
            addToCollectionButton.setStyle("");
        }


        List<PokemonCard> watchlist = cardService.getWatchlist();
        boolean isInWatchlist = watchlist.stream()
                .anyMatch(c -> c.getCardNumber().equals(card.getCardNumber()));

        if (isInWatchlist) {
            addToWatchlistButton.setText("Remove from Watchlist");
            addToWatchlistButton.setStyle("-fx-background-color: #ff6b6b;");
        } else {
            addToWatchlistButton.setText("Add to Watchlist");
            addToWatchlistButton.setStyle("");
        }


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

    private void hideCardDetail() {
        cardDetailBox.setVisible(false);
        cardDetailBox.setManaged(false);
        priceHistoryContainer.setVisible(false);
        priceHistoryContainer.setManaged(false);
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
                case "Set" -> card.getSet();
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
                playButtonClickSound();
                searchField.setText(suggestion);
                searchCards();
            });
            menuItems.add(item);
        }

        autoCompletePopup.getItems().setAll(menuItems);
        autoCompletePopup.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
    }


    private void updatePriceHistoryChart(List<CardPrice> history) {
        priceHistoryChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (CardPrice price : history) {
            String date = price.getTimestamp().toLocalDate().toString();
            series.getData().add(new XYChart.Data<>(date, price.getPrice()));
        }
        series.setName("Price");
        priceHistoryChart.getData().add(series);
    }
}
