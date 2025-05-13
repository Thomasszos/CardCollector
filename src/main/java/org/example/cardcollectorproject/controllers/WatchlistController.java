package org.example.cardcollectorproject.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.cardcollectorproject.api.TCGio;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.services.CardSearching;
import org.example.cardcollectorproject.services.UIAnimationService;
import org.example.cardcollectorproject.services.UserSession;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WatchlistController implements Initializable {

    private static WatchlistController instance;

    @FXML private TilePane watchlistTilePane;
    @FXML private Label emptyWatchlistLabel;
    @FXML private Label userLabel;
    @FXML private TextField searchField;
    @FXML private Label totalPriceLabel;
    @FXML private Label cardCountLabel;
    @FXML private ComboBox<String> filterTypeBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private Button clearSearchButton;
    @FXML private VBox emptyStateBox;
    @FXML private ScrollPane cardScrollPane;

    private final CardSearching cardService = new CardSearching();
    private final UIAnimationService animationService = new UIAnimationService();
    private final Map<String, Double> cardPrices = new ConcurrentHashMap<>();

    // To track how many cards have their prices loaded
    private AtomicInteger cardsProcessed = new AtomicInteger(0);
    private double totalWatchlistValue = 0.0;

    public static WatchlistController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set this controller instance for global access
        instance = this;

        // Make sure the VBox containing all content can be scrolled
        if (watchlistTilePane != null && watchlistTilePane.getParent() != null) {
            VBox parentContainer = (VBox) watchlistTilePane.getParent().getParent();

            // Make sure the parent container doesn't have a fixed height constraint
            parentContainer.setMinHeight(-1);
            parentContainer.setPrefHeight(-1);
            parentContainer.setMaxHeight(Double.MAX_VALUE);

            // Make the scroll pane fill available space and be scrollable
            if (cardScrollPane != null) {
                cardScrollPane.setFitToWidth(true);
                cardScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

                // This critical part ensures scrolling works properly
                VBox.setVgrow(cardScrollPane, Priority.ALWAYS);
            }
        }

        // Setup watchlist filters
        if (filterTypeBox != null) {
            filterTypeBox.getItems().add("All Types");
            filterTypeBox.setValue("All Types");
            filterTypeBox.setOnAction(e -> filterWatchlistByType());
        }

        // Setup sort options
        if (sortBox != null) {
            sortBox.getItems().addAll(
                    "Name (A-Z)",
                    "Name (Z-A)",
                    "Type",
                    "Set",
                    "Price (Low-High)",
                    "Price (High-Low)"
            );
            sortBox.setValue("Name (A-Z)");
            sortBox.setOnAction(e -> sortWatchlist());
        }

        // Setup search functionality
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) ->
                    filterWatchlist(newValue));
        }

        // Setup clear search button
        if (clearSearchButton != null) {
            clearSearchButton.setOnAction(e -> {
                searchField.clear();
                filterWatchlist("");
            });
        }

        // Load the user's watchlist
        setupWatchlistDisplay();
        loadWatchlist();

        // Set username label if available
        if (userLabel != null && UserSession.getInstance().getCurrentUser() != null) {
            userLabel.setText(UserSession.getInstance().getCurrentUser().getUsername() + "'s Watchlist");
        }
    }

    private void filterWatchlistByType() {
        if (filterTypeBox == null) return;

        String selectedType = filterTypeBox.getValue();
        String searchText = searchField.getText().trim().toLowerCase();

        for (javafx.scene.Node node : watchlistTilePane.getChildren()) {
            if (node instanceof VBox) {
                VBox cardBox = (VBox) node;
                PokemonCard card = (PokemonCard) cardBox.getUserData();

                boolean matchesSearch = searchText.isEmpty() ||
                        card.getName().toLowerCase().contains(searchText) ||
                        card.getCardType().toLowerCase().contains(searchText) ||
                        card.getMechanic().toLowerCase().contains(searchText) ||
                        (card.getSet() != null && card.getSet().toLowerCase().contains(searchText));

                boolean matchesType = "All Types".equals(selectedType) ||
                        card.getCardType().equalsIgnoreCase(selectedType);

                boolean shouldShow = matchesSearch && matchesType;

                cardBox.setVisible(shouldShow);
                cardBox.setManaged(shouldShow);
            }
        }
    }

    private void sortWatchlist() {
        if (sortBox == null) return;

        String sortOption = sortBox.getValue();

        // Get cards from UI or watchlist service
        List<PokemonCard> cards = new ArrayList<>();
        for (javafx.scene.Node node : watchlistTilePane.getChildren()) {
            if (node instanceof VBox) {
                VBox cardBox = (VBox) node;
                PokemonCard card = (PokemonCard) cardBox.getUserData();
                cards.add(card);
            }
        }

        // Apply sorting
        switch (sortOption) {
            case "Name (A-Z)":
                cards.sort(Comparator.comparing(PokemonCard::getName));
                break;
            case "Name (Z-A)":
                cards.sort(Comparator.comparing(PokemonCard::getName).reversed());
                break;
            case "Type":
                cards.sort(Comparator.comparing(PokemonCard::getCardType));
                break;
            case "Set":
                cards.sort(Comparator.comparing(card ->
                        card.getSet() != null ? card.getSet() : ""));
                break;
            case "Price (Low-High)":
                sortByPrice(cards, false);
                return; // Async operation, return early
            case "Price (High-Low)":
                sortByPrice(cards, true);
                return; // Async operation, return early
        }

        // Update the UI
        watchlistTilePane.getChildren().clear();
        for (PokemonCard card : cards) {
            VBox cardBox = createCardNode(card);
            watchlistTilePane.getChildren().add(cardBox);
            animationService.applyHoverAnimation(cardBox);
            setupCardRemoval(cardBox, card);
        }
    }

    private void sortByPrice(List<PokemonCard> cards, boolean descending) {
        // We need to fetch prices for all cards before sorting
        Map<String, javafx.concurrent.Task<Double>> priceTasks = new HashMap<>();

        // Start fetching all prices
        for (PokemonCard card : cards) {
            javafx.concurrent.Task<Double> priceTask = new javafx.concurrent.Task<Double>() {
                @Override
                protected Double call() throws Exception {
                    return TCGio.fetchCardPrice(card.getCardNumber()).join();
                }
            };
            priceTasks.put(card.getCardNumber(), priceTask);
            new Thread(priceTask).start();
        }

        // Wait for all tasks to complete
        javafx.concurrent.Task<Void> allPricesTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (javafx.concurrent.Task<Double> task : priceTasks.values()) {
                    try {
                        task.get();
                    } catch (Exception e) {
                        // Handle exceptions
                    }
                }
                return null;
            }
        };

        allPricesTask.setOnSucceeded(event -> {
            // Create a map of card numbers to prices
            Map<String, Double> cardPrices = new HashMap<>();
            for (Map.Entry<String, javafx.concurrent.Task<Double>> entry : priceTasks.entrySet()) {
                try {
                    cardPrices.put(entry.getKey(), entry.getValue().get());
                } catch (Exception e) {
                    cardPrices.put(entry.getKey(), 0.0);
                }
            }

            // Sort the cards by price
            if (descending) {
                cards.sort((c1, c2) -> Double.compare(
                        cardPrices.getOrDefault(c2.getCardNumber(), 0.0),
                        cardPrices.getOrDefault(c1.getCardNumber(), 0.0)
                ));
            } else {
                cards.sort((c1, c2) -> Double.compare(
                        cardPrices.getOrDefault(c1.getCardNumber(), 0.0),
                        cardPrices.getOrDefault(c2.getCardNumber(), 0.0)
                ));
            }

            // Update the UI on the JavaFX thread
            Platform.runLater(() -> {
                watchlistTilePane.getChildren().clear();
                for (PokemonCard card : cards) {
                    VBox cardBox = createCardNode(card);
                    watchlistTilePane.getChildren().add(cardBox);
                    animationService.applyHoverAnimation(cardBox);
                    setupCardRemoval(cardBox, card);
                }
            });
        });

        new Thread(allPricesTask).start();
    }

    private void setupWatchlistDisplay() {
        // Configure the TilePane for better card display
        watchlistTilePane.setPrefColumns(5); // More columns for smaller cards
        watchlistTilePane.setHgap(15);
        watchlistTilePane.setVgap(15);
        watchlistTilePane.setPadding(new Insets(10));

        // If the TilePane is within a ScrollPane, enhance the scroll experience
        if (watchlistTilePane.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) watchlistTilePane.getParent();
            scrollPane.setFitToWidth(true);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setPannable(true); // Allow panning with mouse drag
        }
    }

    public void loadWatchlist() {
        List<PokemonCard> watchlistCards = cardService.getWatchlist();

        // Clear existing cards
        watchlistTilePane.getChildren().clear();

        // Reset watchlist counters
        totalWatchlistValue = 0.0;
        cardsProcessed.set(0);

        // Update the card count
        if (cardCountLabel != null) {
            cardCountLabel.setText("Cards: " + watchlistCards.size());
        }

        // Show empty state or cards
        if (watchlistCards.isEmpty()) {
            if (emptyStateBox != null) {
                emptyStateBox.setVisible(true);
                emptyStateBox.setManaged(true);
            }

            if (emptyWatchlistLabel != null) {
                emptyWatchlistLabel.setVisible(true);
            }

            // Update total price for empty watchlist
            if (totalPriceLabel != null) {
                totalPriceLabel.setText("Total Value: $0.00");
            }
        } else {
            if (emptyStateBox != null) {
                emptyStateBox.setVisible(false);
                emptyStateBox.setManaged(false);
            }

            if (emptyWatchlistLabel != null) {
                emptyWatchlistLabel.setVisible(false);
            }

            // Set total price to calculating state
            if (totalPriceLabel != null) {
                totalPriceLabel.setText("Total Value: Calculating...");
            }

            // Add the watchlist to the tile pane
            for (PokemonCard card : watchlistCards) {
                VBox cardBox = createCardNode(card);
                watchlistTilePane.getChildren().add(cardBox);

                // Apply the hover animation
                animationService.applyHoverAnimation(cardBox);

                // Add remove card functionality
                setupCardRemoval(cardBox, card);
            }

            // Update filter dropdowns with available types
            updateTypeFilterOptions(watchlistCards);
        }

        // Apply any active filters
        if (filterTypeBox != null && !filterTypeBox.getValue().equals("All Types")) {
            filterWatchlistByType();
        }

        // Apply any active sorting
        if (sortBox != null && sortBox.getValue() != null && !sortBox.getValue().equals("Name (A-Z)")) {
            sortWatchlist();
        }
    }

    private void updateTypeFilterOptions(List<PokemonCard> cards) {
        if (filterTypeBox == null) return;

        // Save the current selection
        String currentSelection = filterTypeBox.getValue();

        // Get unique types from the watchlist
        Set<String> types = new TreeSet<>();
        types.add("All Types");

        for (PokemonCard card : cards) {
            if (card.getCardType() != null && !card.getCardType().isEmpty()) {
                types.add(card.getCardType());
            }
        }

        // Update the filter dropdown
        filterTypeBox.getItems().setAll(types);

        // Restore selection or default to "All Types"
        if (types.contains(currentSelection)) {
            filterTypeBox.setValue(currentSelection);
        } else {
            filterTypeBox.setValue("All Types");
        }
    }

    private VBox createCardNode(PokemonCard card) {
        // Create card container
        VBox cardBox = new VBox(8);
        cardBox.setAlignment(Pos.TOP_CENTER);
        cardBox.setPadding(new Insets(15, 15, 10, 15));
        cardBox.setUserData(card);
        cardBox.getStyleClass().add("card-box");
        cardBox.setMinWidth(220);
        cardBox.setMaxWidth(220);
        cardBox.setMinHeight(470);
        cardBox.setMaxHeight(470);

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("card-image-container");
        imageContainer.setMinHeight(320);
        imageContainer.setPrefHeight(320);
        imageContainer.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("card-image");
        imageView.setFitWidth(200);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);

        // Add a subtle drop shadow to the image
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        dropShadow.setRadius(5);
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        imageView.setEffect(dropShadow);

        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(card.getImageUrl(), 200, 300, true, true)); // Match new dimensions
        }

        imageContainer.getChildren().add(imageView);

        // Make the image clickable to show detailed view
        imageView.setOnMouseClicked(e -> {
            showCardDetailView(card);
        });

        // Add a subtle hover effect to indicate the image is clickable
        imageView.setOnMouseEntered(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), imageView);
            scaleTransition.setToX(1.03);
            scaleTransition.setToY(1.03);
            scaleTransition.play();
        });

        imageView.setOnMouseExited(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), imageView);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();

            // Restore original effect
            imageView.setEffect(dropShadow);
        });

        // Card name with better typography
        Label nameLabel = new Label(card.getName());
        nameLabel.getStyleClass().add("card-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190); // Increased max width
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Type label with badge styling
        Label typeLabel = new Label(card.getCardType());
        typeLabel.getStyleClass().add("card-type");

        // Mechanic label with badge styling
        Label mechanicLabel = new Label(card.getMechanic());
        mechanicLabel.getStyleClass().add("card-mechanic");

        // Group type and mechanic in flow pane for flexible layout
        FlowPane tagsContainer = new FlowPane();
        tagsContainer.setHgap(5);
        tagsContainer.setVgap(5);
        tagsContainer.setAlignment(Pos.CENTER);
        tagsContainer.getChildren().addAll(typeLabel, mechanicLabel);

        // Set label with subdued styling
        Label setLabel = new Label(card.getSet() != null ? card.getSet() : "");
        setLabel.getStyleClass().add("card-set");
        setLabel.setWrapText(true);
        setLabel.setMaxWidth(190); // Increased max width
        setLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Price label with distinctive styling
        Label priceLabel = new Label("Price: Loading...");
        priceLabel.getStyleClass().add("card-price");

        // Add a remove button (hidden by default)
        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("remove-button");
        removeButton.setVisible(false);

        // Add a move to collection button
        Button moveToCollectionButton = new Button("Add to Collection");
        moveToCollectionButton.getStyleClass().add("add-to-collection-button");
        moveToCollectionButton.setVisible(false);

        // Fetch price asynchronously
        fetchAndDisplayPrice(card, priceLabel);

        // Add all components to card
        cardBox.getChildren().addAll(
                imageContainer,
                nameLabel,
                tagsContainer,
                setLabel,
                priceLabel,
                moveToCollectionButton,
                removeButton
        );

        // Show/hide buttons on hover
        cardBox.setOnMouseEntered(e -> {
            removeButton.setVisible(true);
            moveToCollectionButton.setVisible(true);

            // a subtle animation when hovering
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), removeButton);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            FadeTransition fadeInMove = new FadeTransition(Duration.millis(200), moveToCollectionButton);
            fadeInMove.setFromValue(0);
            fadeInMove.setToValue(1);
            fadeInMove.play();
        });

        cardBox.setOnMouseExited(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), removeButton);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> removeButton.setVisible(false));
            fadeOut.play();

            FadeTransition fadeOutMove = new FadeTransition(Duration.millis(200), moveToCollectionButton);
            fadeOutMove.setFromValue(1);
            fadeOutMove.setToValue(0);
            fadeOutMove.setOnFinished(event -> moveToCollectionButton.setVisible(false));
            fadeOutMove.play();
        });

        // Handle remove button click
        removeButton.setOnAction(e -> {
            removeCardFromWatchlist(card);
        });

        // Handle move to collection button click
        moveToCollectionButton.setOnAction(e -> {
            moveCardToCollection(card);
        });

        return cardBox;
    }

    private void showCardDetailView(PokemonCard card) {
        try {
            CardDetailView detailView = new CardDetailView();
            Stage detailStage = new Stage();
            detailView.showCardDetail(detailStage, card);
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open card details");
            alert.setContentText("An error occurred while trying to show card details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void setupCardRemoval(VBox cardBox, PokemonCard card) {
        // Add context menu for right-click
        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove from Watchlist");
        MenuItem viewItem = new MenuItem("View Details");
        MenuItem addToCollectionItem = new MenuItem("Add to Collection");

        removeItem.setOnAction(event -> {
            removeCardFromWatchlist(card);
        });

        viewItem.setOnAction(event -> {
            showCardDetailView(card);
        });

        addToCollectionItem.setOnAction(event -> {
            moveCardToCollection(card);
        });

        contextMenu.getItems().addAll(viewItem, addToCollectionItem, removeItem);

        // Show context menu on right-click
        cardBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(cardBox, event.getScreenX(), event.getScreenY());
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // Double-click to view details
                showCardDetailView(card);
            }
        });
    }

    private void removeCardFromWatchlist(PokemonCard card) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Remove Card");
        confirmDialog.setHeaderText("Remove " + card.getName() + " from your watchlist?");
        confirmDialog.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Remove the card from the watchlist
            cardService.removeFromWatchlist(card.getCardNumber());

            // Update watchlist total value
            Double cardPrice = cardPrices.getOrDefault(card.getCardNumber(), 0.0);
            if (cardPrice > 0) {
                totalWatchlistValue -= cardPrice;
                if (totalPriceLabel != null) {
                    totalPriceLabel.setText(String.format("Total Value: $%.2f", totalWatchlistValue));
                }
            }

            // Refresh the watchlist display
            loadWatchlist();
        }
    }

    private void moveCardToCollection(PokemonCard card) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Add to Collection");
        confirmDialog.setHeaderText("Add " + card.getName() + " to your collection?");
        confirmDialog.setContentText("The card will remain in your watchlist.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Add the card to the collection
            cardService.addToCollection(card);

            // Notify the collection controller to refresh
            CollectionController collectionController = CollectionController.getInstance();
            if (collectionController != null) {
                collectionController.updateCollectionAfterAddingCard(card);
            }

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Card Added to Collection");
            successAlert.setContentText(card.getName() + " has been added to your collection.");
            successAlert.showAndWait();
        }
    }

    private void filterWatchlist(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all cards if search is empty
            for (javafx.scene.Node node : watchlistTilePane.getChildren()) {
                node.setVisible(true);
                node.setManaged(true);
            }
            return;
        }

        searchText = searchText.toLowerCase().trim();

        // Filter cards based on search text
        for (javafx.scene.Node node : watchlistTilePane.getChildren()) {
            if (node instanceof VBox) {
                VBox cardBox = (VBox) node;
                PokemonCard card = (PokemonCard) cardBox.getUserData();

                boolean matches = card.getName().toLowerCase().contains(searchText) ||
                        card.getCardType().toLowerCase().contains(searchText) ||
                        card.getMechanic().toLowerCase().contains(searchText) ||
                        (card.getSet() != null && card.getSet().toLowerCase().contains(searchText));

                cardBox.setVisible(matches);
                cardBox.setManaged(matches); // This will reflow the layout
            }
        }
    }

    private void fetchAndDisplayPrice(PokemonCard card, Label priceLabel) {
        // Check if we already have this price cached
        if (cardPrices.containsKey(card.getCardNumber())) {
            Double price = cardPrices.get(card.getCardNumber());
            updatePriceLabel(priceLabel, price);
            updateTotalWatchlistValue(price);
            return;
        }

        TCGio.fetchCardPrice(card.getCardNumber())
                .thenAccept(price -> {
                    // Cache the price
                    cardPrices.put(card.getCardNumber(), price);

                    Platform.runLater(() -> {
                        updatePriceLabel(priceLabel, price);
                        updateTotalWatchlistValue(price);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        priceLabel.setText("Price: Error");
                        updateTotalWatchlistValue(0.0);
                    });
                    return null;
                });
    }

    private void updatePriceLabel(Label priceLabel, Double price) {
        if (price > 0) {
            priceLabel.setText(String.format("Price: $%.2f", price));
        } else {
            priceLabel.setText("Price: N/A");
        }
    }

    private synchronized void updateTotalWatchlistValue(double cardPrice) {
        // Add this card's price to total
        if (cardPrice > 0) {
            totalWatchlistValue += cardPrice;
        }

        // Increment the counter of processed cards
        int processedCount = cardsProcessed.incrementAndGet();

        // If all cards have been processed, update the total price label
        List<PokemonCard> watchlistCards = cardService.getWatchlist();
        if (processedCount >= watchlistCards.size() && totalPriceLabel != null) {
            totalPriceLabel.setText(String.format("Total Value: $%.2f", totalWatchlistValue));
        }
    }

    public void refreshWatchlist() {
        loadWatchlist();
    }

    /**
     * Method to be called when a new card is added to the watchlist
     * This will recalculate the watchlist value
     */
    public void updateWatchlistAfterAddingCard(PokemonCard card) {
        // Reset counters
        cardsProcessed.set(0);
        totalWatchlistValue = 0.0;

        // Fetch the price of the newly added card
        TCGio.fetchCardPrice(card.getCardNumber())
                .thenAccept(price -> {
                    // Cache the price
                    cardPrices.put(card.getCardNumber(), price);

                    // Refresh the entire watchlist to update total value
                    Platform.runLater(this::loadWatchlist);
                })
                .exceptionally(ex -> {
                    Platform.runLater(this::loadWatchlist);
                    return null;
                });
    }
}