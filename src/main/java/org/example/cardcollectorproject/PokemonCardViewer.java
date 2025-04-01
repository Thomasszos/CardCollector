package org.example.cardcollectorproject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PokemonCardViewer extends Application {

    // POJO for storing card details
    public class PokemonCard {
        String name;
        String imageUrl;
        String cardType;
        String mechanic; // e.g., GX, EX, V, etc.
        String moves;
        String cardNumber;

        public PokemonCard(String name, String imageUrl, String cardType, String mechanic, String moves, String cardNumber) {
            this.name = name;
            this.imageUrl = imageUrl;
            this.cardType = cardType;
            this.mechanic = mechanic;
            this.moves = moves;
            this.cardNumber = cardNumber;
        }
    }

    // List to hold card data
    private List<PokemonCard> cards = new ArrayList<>();
    // ListView to display card items
    private ListView<HBox> listView = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        // ComboBox for sorting options (by Name or Type)
        ComboBox<String> sortOptions = new ComboBox<>();
        sortOptions.getItems().addAll("Name", "Type");
        sortOptions.setValue("Name");  // default sort by name

        sortOptions.setOnAction(e -> {
            String selected = sortOptions.getValue();
            sortAndDisplayCards(selected);
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(sortOptions, listView);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Pokemon Card Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Fetch data from the API
        fetchDataFromAPI();
    }

    // Fetch data from the Pokémon TCG API asynchronously
    private void fetchDataFromAPI() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.pokemontcg.io/v2/cards"))
                .header("X-Api-Key", "6c90b57d-f700-4186-bee3-997e33923403")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::parseJson)
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    // Parse JSON response and populate the cards list
    private void parseJson(String responseBody) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        JsonArray data = json.getAsJsonArray("data");

        // Loop through each card in the JSON array
        for (int i = 0; i < data.size(); i++) {
            JsonObject cardJson = data.get(i).getAsJsonObject();

            // Extract card details; adjust field names as needed based on API response
            String name = cardJson.has("name") ? cardJson.get("name").getAsString() : "Unknown";
            String imageUrl = "";
            if (cardJson.has("images")) {
                JsonObject images = cardJson.getAsJsonObject("images");
                imageUrl = images.has("large") ? images.get("large").getAsString() : "";
            }
            String cardType = "";
            if (cardJson.has("types")) {
                cardType = cardJson.get("types").getAsJsonArray().toString();
            }
            String mechanic = cardJson.has("subtypes") ? cardJson.get("subtypes").getAsJsonArray().toString() : "N/A";
            String moves = "";
            if (cardJson.has("attacks")) {
                JsonArray attacks = cardJson.getAsJsonArray("attacks");
                StringBuilder movesBuilder = new StringBuilder();
                attacks.forEach(attack -> movesBuilder.append(attack.getAsJsonObject().get("name").getAsString()).append(" "));
                moves = movesBuilder.toString().trim();
            }
            String cardNumber = "";
            if (cardJson.has("number")) {
                cardNumber = cardJson.get("number").getAsString();
            }

            PokemonCard card = new PokemonCard(name, imageUrl, cardType, mechanic, moves, cardNumber);
            cards.add(card);
        }
        // After parsing, sort by the default criterion
        Platform.runLater(() -> sortAndDisplayCards("Name"));
    }

    // Sort cards based on the selected criterion and update the ListView
    private void sortAndDisplayCards(String criterion) {
        if ("Name".equals(criterion)) {
            cards.sort(Comparator.comparing(c -> c.name));
        } else if ("Type".equals(criterion)) {
            cards.sort(Comparator.comparing(c -> c.cardType));
        }

        listView.getItems().clear();
        for (PokemonCard card : cards) {
            listView.getItems().add(createCardHBox(card));
        }
    }

    // Create an HBox for a single card item
    private HBox createCardHBox(PokemonCard card) {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(5));

        ImageView imageView;
        if (!card.imageUrl.isEmpty()) {
            // Create an ImageView with a fixed width (height auto-scaled)
            imageView = new ImageView(new Image(card.imageUrl, 150, 0, true, true));
        } else {
            imageView = new ImageView();
        }

        VBox infoBox = new VBox(5);
        infoBox.getChildren().add(new Label("Name: " + card.name));
        infoBox.getChildren().add(new Label("Type: " + card.cardType));
        infoBox.getChildren().add(new Label("Mechanic: " + card.mechanic));
        infoBox.getChildren().add(new Label("Moves: " + card.moves));
        infoBox.getChildren().add(new Label("Card Number: " + card.cardNumber));

        hbox.getChildren().addAll(imageView, infoBox);
        return hbox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

