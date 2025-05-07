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
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.cardcollectorproject.api.TCGio;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.services.CardSearching;
import org.example.cardcollectorproject.services.UIAnimationService;
import org.example.cardcollectorproject.services.UserSession;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CollectionController implements Initializable {

    private static CollectionController instance;

    @FXML private TilePane collectionTilePane;
    @FXML private Label emptyCollectionLabel;
    @FXML private Label userLabel;
    @FXML private TextField searchField;
    @FXML private Label totalPriceLabel;
    @FXML private Label cardCountLabel;
    @FXML private ImageView collectionImage;
    @FXML private ComboBox<String> filterTypeBox;
    @FXML private ComboBox<String> sortBox;
    @FXML private ToggleButton gridViewToggle;
    @FXML private ToggleButton listViewToggle;
    @FXML private ListView<PokemonCard> collectionListView;
    @FXML private Button clearSearchButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button statsButton;
    @FXML private VBox emptyStateBox;
    @FXML private ScrollPane cardScrollPane;

    private final CardSearching cardService = new CardSearching();
    private final UIAnimationService animationService = new UIAnimationService();
    private final Map<String, Double> cardPrices = new ConcurrentHashMap<>();



    // To track how many cards have their prices loaded
    private AtomicInteger cardsProcessed = new AtomicInteger(0);
    private double totalCollectionValue = 0.0;

    public static CollectionController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set this controller instance for global access
        instance = this;

        // Make sure the VBox containing all content can be scrolled
        // by ensuring it's in a ScrollPane with proper settings
        if (collectionTilePane != null && collectionTilePane.getParent() != null) {
            VBox parentContainer = (VBox) collectionTilePane.getParent().getParent();

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

        // Setup collection filters
        if (filterTypeBox != null) {
            filterTypeBox.getItems().add("All Types");
            filterTypeBox.setValue("All Types");
            filterTypeBox.setOnAction(e -> filterCollectionByType());
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
            sortBox.setOnAction(e -> sortCollection());
        }

        // Setup search functionality
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) ->
                    filterCollection(newValue));
        }

        // Setup clear search button
        if (clearSearchButton != null) {
            clearSearchButton.setOnAction(e -> {
                searchField.clear();
                filterCollection("");
            });
        }

//        // Setup view toggle buttons
//        if (gridViewToggle != null && listViewToggle != null) {
//            // Create a toggle group so only one can be selected
//            ToggleGroup viewToggle = new ToggleGroup();
//            gridViewToggle.setToggleGroup(viewToggle);
//            listViewToggle.setToggleGroup(viewToggle);
//
//            // Set initial view (grid view is default)
//            gridViewToggle.setSelected(true);
//            switchToGridView();
//
//            // Set up event handlers
//            gridViewToggle.setOnAction(e -> switchToGridView());
//            listViewToggle.setOnAction(e -> switchToListView());


//        // Initialize ListView but keep it hidden initially
//        if (collectionListView != null) {
//            collectionListView.setVisible(false);
//            collectionListView.setManaged(false);
//
//            // Set fill width to ensure it takes up available space
//            VBox.setVgrow(collectionListView, Priority.ALWAYS);
//        }
//
//        // Setup refresh button
//        if (refreshButton != null) {
//            refreshButton.setOnAction(e -> refreshCollectionPrices());
//        }
//
//        // Setup export button
//        if (exportButton != null) {
//            exportButton.setOnAction(e -> exportCollection());
//        }
//
       // Setup stats button
        if (statsButton != null) {
            statsButton.setOnAction(e -> showCollectionStats());
        }

        // Load the user's collection
        setupCollectionDisplay();
        loadCollection();

        // Set username label if available
        if (userLabel != null && UserSession.getInstance().getCurrentUser() != null) {
            userLabel.setText(UserSession.getInstance().getCurrentUser().getUsername() + "'s Collection");
        }
    }
//    /**
//     * Switches the collection display to grid view mode.
//     * This method shows the card scroll pane with the tile layout and hides the list view.
//     */
//    private void switchToGridView() {
//        if (cardScrollPane != null && collectionListView != null) {
//            // Show the grid view (tile pane inside scroll pane)
//            cardScrollPane.setVisible(true);
//            cardScrollPane.setManaged(true);
//
//            // Hide the list view
//            collectionListView.setVisible(false);
//            collectionListView.setManaged(false);
//
//            // Ensure the toggle buttons reflect the correct state
//            if (gridViewToggle != null && !gridViewToggle.isSelected()) {
//                gridViewToggle.setSelected(true);
//            }
//            if (listViewToggle != null && listViewToggle.isSelected()) {
//                listViewToggle.setSelected(false);
//            }
//        }
//    }
//
//    /**
//     * Switches the collection display to list view mode.
//     * This method shows the list view and hides the card scroll pane with the tile layout.
//     */
//    private void switchToListView() {
//        if (cardScrollPane != null && collectionListView != null) {
//            // Hide the grid view
//            cardScrollPane.setVisible(false);
//            cardScrollPane.setManaged(false);
//
//            // Show the list view
//            collectionListView.setVisible(true);
//            collectionListView.setManaged(true);
//
//            // Ensure the toggle buttons reflect the correct state
//            if (gridViewToggle != null && gridViewToggle.isSelected()) {
//                gridViewToggle.setSelected(false);
//            }
//            if (listViewToggle != null && !listViewToggle.isSelected()) {
//                listViewToggle.setSelected(true);
//            }
//
//            // If we haven't set up the list view yet or it's empty, populate it
//            if (collectionListView.getItems().isEmpty()) {
//                setupListView();
//            }
//        }
//    }
    private void setupListView() {
        // Configure the list view for card display
        collectionListView.setCellFactory(lv -> new ListCell<PokemonCard>() {
            private final HBox container = new HBox(15);
            private final ImageView imageView = new ImageView();
            private final VBox detailsBox = new VBox(5);
            private final Label nameLabel = new Label();
            private final Label typeLabel = new Label();
            private final Label priceLabel = new Label();
            private final Button removeButton = new Button("Remove");

            {
                // Configure components
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(10));

                imageView.setFitHeight(60);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);

                nameLabel.getStyleClass().add("card-name");
                typeLabel.getStyleClass().add("card-type");
                priceLabel.getStyleClass().add("card-price");
                removeButton.getStyleClass().add("remove-button");

                detailsBox.getChildren().addAll(nameLabel, typeLabel, priceLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                container.getChildren().addAll(imageView, detailsBox, spacer, removeButton);

                // Set CSS styles for list item
                container.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 8;");

                // Setup hover effect
                container.setOnMouseEntered(e -> {
                    container.setStyle("-fx-background-color: rgba(255, 255, 255, 1.0); -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(76, 175, 80, 0.5), 10, 0, 0, 0);");
                });

                container.setOnMouseExited(e -> {
                    container.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 8;");
                });
            }

            @Override
            protected void updateItem(PokemonCard card, boolean empty) {
                super.updateItem(card, empty);

                if (empty || card == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(card.getName());
                    typeLabel.setText(card.getCardType() + " • " + card.getMechanic());

                    if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
                        imageView.setImage(new Image(card.getImageUrl(), 40, 60, true, true));
                    }

                    // Set price (fetch asynchronously)
                    priceLabel.setText("Price: Loading...");
                    TCGio.fetchCardPrice(card.getCardNumber())
                            .thenAccept(price -> {
                                Platform.runLater(() -> {
                                    if (price > 0) {
                                        priceLabel.setText(String.format("$%.2f", price));
                                    } else {
                                        priceLabel.setText("Price N/A");
                                    }
                                });
                            });

                    // Handle remove button action
                    removeButton.setOnAction(e -> {
                        removeCardFromCollection(card);
                    });

                    setGraphic(container);
                }
            }
        });

        // Load the cards into the list view
        List<PokemonCard> cards = cardService.getCollection();
        collectionListView.getItems().addAll(cards);
    }

    private void filterCollectionByType() {
        if (filterTypeBox == null) return;

        String selectedType = filterTypeBox.getValue();
        String searchText = searchField.getText().trim().toLowerCase();

        for (javafx.scene.Node node : collectionTilePane.getChildren()) {
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


        if (collectionListView != null && collectionListView.isVisible()) {
            refreshListViewFiltering();
        }
    }

    private void refreshListViewFiltering() {
        if (collectionListView == null) return;

        String selectedType = filterTypeBox.getValue();
        String searchText = searchField.getText().trim().toLowerCase();

        // Get the full collection
        List<PokemonCard> allCards = cardService.getCollection();

        // Apply filters
        List<PokemonCard> filteredCards = allCards.stream()
                .filter(card -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            card.getName().toLowerCase().contains(searchText) ||
                            card.getCardType().toLowerCase().contains(searchText) ||
                            card.getMechanic().toLowerCase().contains(searchText) ||
                            (card.getSet() != null && card.getSet().toLowerCase().contains(searchText));

                    boolean matchesType = "All Types".equals(selectedType) ||
                            card.getCardType().equalsIgnoreCase(selectedType);

                    return matchesSearch && matchesType;
                })
                .collect(Collectors.toList());

        // Update the list view
        collectionListView.getItems().setAll(filteredCards);
    }

    private void sortCollection() {
        if (sortBox == null) return;

        String sortOption = sortBox.getValue();

        // Get cards from UI or collection service
        List<PokemonCard> cards = new ArrayList<>();
        for (javafx.scene.Node node : collectionTilePane.getChildren()) {
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
        collectionTilePane.getChildren().clear();
        for (PokemonCard card : cards) {
            VBox cardBox = createCardNode(card);
            collectionTilePane.getChildren().add(cardBox);
            animationService.applyHoverAnimation(cardBox);
            setupCardRemoval(cardBox, card);
        }

        // Also update list view if it's visible
        if (collectionListView != null && collectionListView.isVisible()) {
            collectionListView.getItems().setAll(cards);
        }
    }

    private void sortByPrice(List<PokemonCard> cards, boolean descending) {
        // We need to fetch prices for all cards before sorting
        Map<String, CompletableFuture<Double>> priceFutures = new HashMap<>();

        // Start fetching all prices
        for (PokemonCard card : cards) {
            priceFutures.put(card.getCardNumber(), TCGio.fetchCardPrice(card.getCardNumber()));
        }

        // Combine all futures
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                priceFutures.values().toArray(new CompletableFuture[0])
        );

        // When all prices are fetched, sort the cards
        allFutures.thenRun(() -> {
            // Create a map of card numbers to prices
            Map<String, Double> cardPrices = new HashMap<>();
            for (Map.Entry<String, CompletableFuture<Double>> entry : priceFutures.entrySet()) {
                cardPrices.put(entry.getKey(), entry.getValue().join());
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
                collectionTilePane.getChildren().clear();
                for (PokemonCard card : cards) {
                    VBox cardBox = createCardNode(card);
                    collectionTilePane.getChildren().add(cardBox);
                    animationService.applyHoverAnimation(cardBox);
                    setupCardRemoval(cardBox, card);
                }

                // Also update list view if it's visible
                if (collectionListView != null && collectionListView.isVisible()) {
                    collectionListView.getItems().setAll(cards);
                }
            });
        });
    }

//    private void refreshCollectionPrices() {
//        // Show a loading indicator
//        totalPriceLabel.setText("Recalculating values...");
//
//        // Reset counters
//        totalCollectionValue = 0.0;
//        cardsProcessed.set(0);
//        cardPrices.clear();
//
//        // Reload the collection with fresh prices
//        List<PokemonCard> cards = cardService.getCollection();
//
//        // Create a list of futures for all price fetches
//        List<CompletableFuture<Double>> priceFutures = new ArrayList<>();
//
//        for (PokemonCard card : cards) {
//            CompletableFuture<Double> priceFuture = TCGio.fetchCardPrice(card.getCardNumber())
//                    .thenApply(price -> {
//                        // Cache the price
//                        cardPrices.put(card.getCardNumber(), price);
//                        return price;
//                    });
//
//            priceFutures.add(priceFuture);
//        }
//
//        // When all prices are fetched, update the total
//        CompletableFuture.allOf(priceFutures.toArray(new CompletableFuture[0]))
//                .thenRun(() -> {
//                    // Calculate total value
//                    double total = cardPrices.values().stream()
//                            .mapToDouble(Double::doubleValue)
//                            .sum();
//
//                    // Update UI on JavaFX thread
//                    Platform.runLater(() -> {
//                        totalCollectionValue = total;
//                        totalPriceLabel.setText(String.format("Total Value: $%.2f", total));
//
//                        // Refresh the collection display with new prices
//                        loadCollection();
//                    });
//                });
//    }

//    private void exportCollection() {
//        List<PokemonCard> collection = cardService.getCollection();
//
//        if (collection.isEmpty()) {
//            showAlert(Alert.AlertType.INFORMATION,
//                    "Export Collection",
//                    "Your collection is empty",
//                    "Add some cards to your collection before exporting.");
//            return;
//        }
//
//        // Create export choices dialog
//        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
//        dialog.setTitle("Export Collection");
//        dialog.setHeaderText("Choose Export Format");
//        dialog.setContentText("Select the format for your collection export:");
//
//        ButtonType csvButton = new ButtonType("CSV");
//        ButtonType jsonButton = new ButtonType("JSON");
//        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
//
//        dialog.getButtonTypes().setAll(csvButton, jsonButton, cancelButton);
//
//        Optional<ButtonType> result = dialog.showAndWait();
//
//        if (result.isPresent()) {
//            if (result.get() == csvButton) {
//                exportCollectionToCSV(collection);
//            } else if (result.get() == jsonButton) {
//                exportCollectionToJSON(collection);
//            }
//        }
//    }

//    private void exportCollectionToCSV(List<PokemonCard> collection) {
//        // Implementation for CSV export
//        // This would include file chooser and writing the collection to CSV
//        // Placeholder implementation:
//        showAlert(Alert.AlertType.INFORMATION,
//                "Export Complete",
//                "Collection exported to CSV",
//                "Your collection has been exported successfully.");
//    }
//
//    private void exportCollectionToJSON(List<PokemonCard> collection) {
//        // Implementation for JSON export
//        // This would include file chooser and writing the collection to JSON
//        // Placeholder implementation:
//        showAlert(Alert.AlertType.INFORMATION,
//                "Export Complete",
//                "Collection exported to JSON",
//                "Your collection has been exported successfully.");
//    }

    private void showCollectionStats() {
        List<PokemonCard> collection = cardService.getCollection();

        if (collection.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Collection Statistics",
                    "Your collection is empty",
                    "Add some cards to your collection to view statistics.");
            return;
        }

        // Count cards by type
        Map<String, Long> typeCount = collection.stream()
                .collect(Collectors.groupingBy(PokemonCard::getCardType, Collectors.counting()));

        // Count cards by mechanic
        Map<String, Long> mechanicCount = collection.stream()
                .collect(Collectors.groupingBy(PokemonCard::getMechanic, Collectors.counting()));

        // Find most valuable card
        PokemonCard mostValuableCard = collection.stream()
                .max(Comparator.comparingDouble(card -> cardPrices.getOrDefault(card.getCardNumber(), 0.0)))
                .orElse(null);

        // Create a stats display
        Stage statsStage = new Stage();
        statsStage.setTitle("Collection Statistics");

        // Use a dark background to match the main app theme
        VBox statsBox = new VBox(20);
        statsBox.setPadding(new Insets(25));
        statsBox.setStyle("-fx-background-color: #2c2c2c;"); // Dark background that matches the card box style

        Label titleLabel = new Label("Collection Statistics");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;"); // White text for contrast
        titleLabel.setAlignment(Pos.CENTER);

        // Basic stats section - using lighter background with dark text for contrast
        VBox basicStats = new VBox(10);

        // Create and style labels consistently
        Label totalCardsLabel = new Label("Total Cards: " + collection.size());
        totalCardsLabel.setStyle("-fx-text-fill: #333333;");

        Label totalValueLabel = new Label(String.format("Total Value: $%.2f", totalCollectionValue));
        totalValueLabel.setStyle("-fx-text-fill: #333333;");

        Label avgValueLabel = new Label(String.format("Average Card Value: $%.2f",
                collection.isEmpty() ? 0.0 : totalCollectionValue / collection.size()));
        avgValueLabel.setStyle("-fx-text-fill: #333333;");

        basicStats.getChildren().addAll(totalCardsLabel, totalValueLabel, avgValueLabel);
        basicStats.setStyle("-fx-padding: 15; -fx-background-color: #e0e0e0; -fx-background-radius: 8;");

        // Type distribution section
        VBox typeStats = new VBox(8);
        Label typesHeaderLabel = new Label("Card Types:");
        typesHeaderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
        typeStats.getChildren().add(typesHeaderLabel);

        // Add a bar chart for types
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / collection.size();

            Label typeLabel = new Label(entry.getKey() + ": " + entry.getValue() +
                    String.format(" (%.1f%%)", percentage));
            typeLabel.setStyle("-fx-text-fill: #333333;");

            ProgressBar bar = new ProgressBar(percentage / 100.0);
            bar.setPrefWidth(300);
            bar.setStyle("-fx-accent: " + getColorForType(entry.getKey()) + ";");

            HBox typeRow = new HBox(10, typeLabel, bar);
            typeRow.setAlignment(Pos.CENTER_LEFT);

            typeStats.getChildren().add(typeRow);
        }
        typeStats.setStyle("-fx-padding: 15; -fx-background-color: #e0e0e0; -fx-background-radius: 8;");

        // Most valuable card section
        VBox valuableCardBox = new VBox(10);
        Label valuableCardHeaderLabel = new Label("Most Valuable Card:");
        valuableCardHeaderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
        valuableCardBox.getChildren().add(valuableCardHeaderLabel);

        if (mostValuableCard != null) {
            HBox cardInfo = new HBox(15);

            ImageView cardImage = new ImageView();
            if (mostValuableCard.getImageUrl() != null) {
                cardImage.setImage(new Image(mostValuableCard.getImageUrl(), 80, 0, true, true));
            }

            VBox cardDetails = new VBox(5);

            Label cardNameLabel = new Label(mostValuableCard.getName());
            cardNameLabel.setStyle("-fx-text-fill: #333333; -fx-font-weight: bold;");

            Label cardTypeLabel = new Label("Type: " + mostValuableCard.getCardType());
            cardTypeLabel.setStyle("-fx-text-fill: #333333;");

            Label cardValueLabel = new Label(String.format("Value: $%.2f",
                    cardPrices.getOrDefault(mostValuableCard.getCardNumber(), 0.0)));
            cardValueLabel.setStyle("-fx-text-fill: #333333;");

            cardDetails.getChildren().addAll(cardNameLabel, cardTypeLabel, cardValueLabel);

            cardInfo.getChildren().addAll(cardImage, cardDetails);
            valuableCardBox.getChildren().add(cardInfo);
        } else {
            Label noDataLabel = new Label("No card value data available");
            noDataLabel.setStyle("-fx-text-fill: #333333;");
            valuableCardBox.getChildren().add(noDataLabel);
        }
        valuableCardBox.setStyle("-fx-padding: 15; -fx-background-color: #e0e0e0; -fx-background-radius: 8;");

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> statsStage.close());
        closeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20;");
        closeButton.setPrefWidth(150);

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 20, 0));

        // Add all sections to the main container
        statsBox.getChildren().addAll(
                titleLabel,
                basicStats,
                typeStats,
                valuableCardBox,
                buttonBox
        );

        // Create a scroll pane that will properly handle content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(statsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        // Configure scrollbar styling
        scrollPane.getStyleClass().add("stats-scroll-pane");

        // Make sure the scroll pane's viewport background matches our desired color
        scrollPane.setStyle("-fx-background: #2c2c2c; -fx-background-color: #2c2c2c;");

        // Create main scene
        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(scrollPane);
        mainLayout.setStyle("-fx-background-color: #2c2c2c;");

        Scene scene = new Scene(mainLayout, 500, 600);


        try {
            // Get the current collection.css resource path
            String cssPath = getClass().getResource("/org/example/cardcollectorproject/styles/collection.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            // If the CSS can't be loaded, continue without it
            System.err.println("Could not load collection.css: " + e.getMessage());
        }

        // Also add this CSS style to override the viewport background
        scene.getStylesheets().add("data:text/css," +
                ".stats-scroll-pane > .viewport { -fx-background-color: #2c2c2c; }");

        statsStage.setScene(scene);
        statsStage.show();
    }

    private String getColorForType(String type) {
        // Return color hex values based on Pokémon type
        return switch (type.toLowerCase()) {
            case "fire" -> "#FF5722";
            case "water" -> "#2196F3";
            case "grass" -> "#4CAF50";
            case "electric" -> "#FFEB3B";
            case "psychic" -> "#9C27B0";
            case "fighting" -> "#795548";
            case "darkness", "dark" -> "#424242";
            case "metal" -> "#9E9E9E";
            case "dragon" -> "#FF9800";
            case "fairy" -> "#E91E63";
            default -> "#607D8B";
        };
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupCollectionDisplay() {
        // Configure the TilePane for better card display
        collectionTilePane.setPrefColumns(5); // More columns for smaller cards
        collectionTilePane.setHgap(15);
        collectionTilePane.setVgap(15);
        collectionTilePane.setPadding(new Insets(10));

        // If the TilePane is within a ScrollPane, enhance the scroll experience
        if (collectionTilePane.getParent() instanceof ScrollPane) {
            ScrollPane scrollPane = (ScrollPane) collectionTilePane.getParent();
            scrollPane.setFitToWidth(true);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setPannable(true); // Allow panning with mouse drag
        }
    }

    public void loadCollection() {
        List<PokemonCard> collectionCards = cardService.getCollection();

        // Clear existing cards
        collectionTilePane.getChildren().clear();

        // Reset collection counters
        totalCollectionValue = 0.0;
        cardsProcessed.set(0);

        // Update the card count
        if (cardCountLabel != null) {
            cardCountLabel.setText("Cards: " + collectionCards.size());
        }

        // Show empty state or cards
        if (collectionCards.isEmpty()) {
            if (emptyStateBox != null) {
                emptyStateBox.setVisible(true);
                emptyStateBox.setManaged(true);
            }

            if (emptyCollectionLabel != null) {
                emptyCollectionLabel.setVisible(true);
            }

            // Update total price for empty collection
            if (totalPriceLabel != null) {
                totalPriceLabel.setText("Total Value: $0.00");
            }
        } else {
            if (emptyStateBox != null) {
                emptyStateBox.setVisible(false);
                emptyStateBox.setManaged(false);
            }

            if (emptyCollectionLabel != null) {
                emptyCollectionLabel.setVisible(false);
            }

            // Set total price to calculating state
            if (totalPriceLabel != null) {
                totalPriceLabel.setText("Total Value: Calculating...");
            }

            // Add the collection to the tile pane
            for (PokemonCard card : collectionCards) {
                VBox cardBox = createCardNode(card);
                collectionTilePane.getChildren().add(cardBox);

                // Apply the hover animation
                animationService.applyHoverAnimation(cardBox);

                // Add remove card functionality
                setupCardRemoval(cardBox, card);
            }

            if (collectionListView != null && collectionListView.isVisible()) {

                if (collectionListView.getItems().isEmpty()) {
                    setupListView(); // Full setup if first time
                } else {
                    collectionListView.getItems().setAll(collectionCards); // Just update data otherwise
                }
            }


            if (collectionListView != null) {
                collectionListView.getItems().setAll(collectionCards);
            }

            // Update filter dropdowns with available types
            updateTypeFilterOptions(collectionCards);
        }

        // Apply any active filters
        if (filterTypeBox != null && !filterTypeBox.getValue().equals("All Types")) {
            filterCollectionByType();
        }

        // Apply any active sorting
        if (sortBox != null && sortBox.getValue() != null && !sortBox.getValue().equals("Name (A-Z)")) {
            sortCollection();
        }
    }

    private void updateTypeFilterOptions(List<PokemonCard> cards) {
        if (filterTypeBox == null) return;

        // Save the current selection
        String currentSelection = filterTypeBox.getValue();

        // Get unique types from the collection
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

        // Fetch price asynchronously
        fetchAndDisplayPrice(card, priceLabel);

        // Add all components to card
        cardBox.getChildren().addAll(
                imageContainer,
                nameLabel,
                tagsContainer,
                setLabel,
                priceLabel,
                removeButton
        );

        // Show/hide remove button on hover
        cardBox.setOnMouseEntered(e -> {
            removeButton.setVisible(true);

            // a subtle animation when hovering
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), removeButton);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });

        cardBox.setOnMouseExited(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), removeButton);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> removeButton.setVisible(false));
            fadeOut.play();
        });

        // Handle remove button click
        removeButton.setOnAction(e -> {
            removeCardFromCollection(card);
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

    private void showCardDetails(PokemonCard card) {
        Stage detailStage = new Stage();
        detailStage.setTitle(card.getName() + " Details");

        VBox detailBox = new VBox(15);
        detailBox.setPadding(new Insets(20));
        detailBox.setAlignment(Pos.CENTER);
        detailBox.setStyle("-fx-background-color: white;");

        // Card image
        ImageView imageView = new ImageView();
        if (card.getImageUrl() != null && !card.getImageUrl().isEmpty()) {
            imageView.setImage(new Image(card.getImageUrl(), 300, 0, true, true));
        }

        // Card details
        Label nameLabel = new Label("Name: " + card.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nameLabel.setWrapText(true);

        Label typeLabel = new Label("Type: " + card.getCardType());
        typeLabel.setFont(Font.font("System", 14));

        Label mechanicLabel = new Label("Class: " + card.getMechanic());
        mechanicLabel.setFont(Font.font("System", 14));

        Label movesLabel = new Label("Moves: " + (card.getMoves() != null ? card.getMoves() : "N/A"));
        movesLabel.setFont(Font.font("System", 14));
        movesLabel.setWrapText(true);

        Label setLabel = new Label("Set: " + (card.getSet() != null ? card.getSet() : "Loading..."));
        setLabel.setFont(Font.font("System", 14));

        Label cardNumberLabel = new Label("Card #: " + card.getCardNumber());
        cardNumberLabel.setFont(Font.font("System", 14));

        Label priceLabel = new Label("Market Price: Loading...");
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Fetch price
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
                    Platform.runLater(() -> priceLabel.setText("Market Price: error"));
                    return null;
                });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> detailStage.close());
        closeButton.setPrefWidth(100);

        // Add all components to detail box
        detailBox.getChildren().addAll(
                imageView,
                nameLabel,
                typeLabel,
                mechanicLabel,
                movesLabel,
                setLabel,
                cardNumberLabel,
                priceLabel,
                closeButton
        );

        Scene scene = new Scene(detailBox, 400, 650);
        detailStage.setScene(scene);
        detailStage.show();
    }

    private void setupCardRemoval(VBox cardBox, PokemonCard card) {
        // Add context menu for right-click
        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove from Collection");
        MenuItem viewItem = new MenuItem("View Details");

        removeItem.setOnAction(event -> {
            removeCardFromCollection(card);
        });

        viewItem.setOnAction(event -> {
            // You could implement a detailed view here
            showCardDetails(card);
        });

        contextMenu.getItems().addAll(viewItem, removeItem);

        // Show context menu on right-click
        cardBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(cardBox, event.getScreenX(), event.getScreenY());
            } else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                // Double-click to view details
                showCardDetails(card);
            }
        });
    }

    private void removeCardFromCollection(PokemonCard card) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Remove Card");
        confirmDialog.setHeaderText("Remove " + card.getName() + " from your collection?");
        confirmDialog.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Remove the card from the collection
            cardService.removeFromCollection(card.getCardNumber());

            // Update collection total value
            Double cardPrice = cardPrices.getOrDefault(card.getCardNumber(), 0.0);
            if (cardPrice > 0) {
                totalCollectionValue -= cardPrice;
                if (totalPriceLabel != null) {
                    totalPriceLabel.setText(String.format("Total Value: $%.2f", totalCollectionValue));
                }
            }

            // Refresh the collection display
            loadCollection();
        }
    }

    private void filterCollection(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all cards if search is empty
            for (javafx.scene.Node node : collectionTilePane.getChildren()) {
                node.setVisible(true);
                node.setManaged(true);
            }
            return;
        }

        searchText = searchText.toLowerCase().trim();

        // Filter cards based on search text
        for (javafx.scene.Node node : collectionTilePane.getChildren()) {
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
            updateTotalCollectionValue(price);
            return;
        }

        TCGio.fetchCardPrice(card.getCardNumber())
                .thenAccept(price -> {
                    // Cache the price
                    cardPrices.put(card.getCardNumber(), price);

                    Platform.runLater(() -> {
                        updatePriceLabel(priceLabel, price);
                        updateTotalCollectionValue(price);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        priceLabel.setText("Price: Error");
                        updateTotalCollectionValue(0.0);
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

    private synchronized void updateTotalCollectionValue(double cardPrice) {
        // Add this card's price to total
        if (cardPrice > 0) {
            totalCollectionValue += cardPrice;
        }

        // Increment the counter of processed cards
        int processedCount = cardsProcessed.incrementAndGet();

        // If all cards have been processed, update the total price label
        List<PokemonCard> collectionCards = cardService.getCollection();
        if (processedCount >= collectionCards.size() && totalPriceLabel != null) {
            totalPriceLabel.setText(String.format("Total Value: $%.2f", totalCollectionValue));
        }
    }

    public void refreshCollection() {
        loadCollection();
    }

    /**
     * Method to be called when a new card is added to the collection
     * This will recalculate the collection value
     */
    public void updateCollectionAfterAddingCard(PokemonCard card) {
        // Reset counters
        cardsProcessed.set(0);
        totalCollectionValue = 0.0;

        // Fetch the price of the newly added card
        TCGio.fetchCardPrice(card.getCardNumber())
                .thenAccept(price -> {
                    // Cache the price
                    cardPrices.put(card.getCardNumber(), price);

                    // Refresh the entire collection to update total value
                    Platform.runLater(this::loadCollection);
                })
                .exceptionally(ex -> {
                    Platform.runLater(this::loadCollection);
                    return null;
                });
    }
}