package org.example.cardcollectorproject.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.example.cardcollectorproject.models.PokemonCard;
import org.example.cardcollectorproject.models.UserCollection;

import java.util.stream.Collectors;


import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CardSearching {

    private final CosmosDbService dbService;

    public CardSearching() {
        this.dbService = new CosmosDbService();
    }

    public void addToCollection(PokemonCard card) {
        // Get the current user
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String userId = session.getCurrentUser().getUserId();
            dbService.addCardToUserCollection(userId, "collection", card);
        } else {
            System.err.println("No user logged in. Cannot add card to collection.");
        }
    }

    public void addToWatchlist(PokemonCard card) {
        // Get the current user
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String userId = session.getCurrentUser().getUserId();
            dbService.addCardToUserCollection(userId, "watchlist", card);
        } else {
            System.err.println("No user logged in. Cannot add card to watchlist.");
        }
    }

    public List<PokemonCard> getCollection() {
        // Get the current user
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String userId = session.getCurrentUser().getUserId();
            UserCollection collection = dbService.getUserCollection(userId, "collection");
            return collection.getCards();
        } else {
            System.err.println("No user logged in. Cannot retrieve collection.");
            return new ArrayList<>();
        }
    }

    public List<PokemonCard> getWatchlist() {
        // Get the current user
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String userId = session.getCurrentUser().getUserId();
            UserCollection watchlist = dbService.getUserCollection(userId, "watchlist");
            return watchlist.getCards();
        } else {
            System.err.println("No user logged in. Cannot retrieve watchlist.");
            return new ArrayList<>();
        }
    }

    public void removeFromCollection(String cardNumber) {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String userId = session.getCurrentUser().getUserId();
            dbService.removeCardFromUserCollection(userId, "collection", cardNumber);
        } else {
            System.err.println("No user logged in. Cannot remove card from collection.");
        }
    }

    public void removeFromWatchlist(String cardNumber) {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            String userId = session.getCurrentUser().getUserId();
            dbService.removeCardFromUserCollection(userId, "watchlist", cardNumber);
        } else {
            System.err.println("No user logged in. Cannot remove card from watchlist.");
        }
    }


    // New method with all search parameters
    public List<PokemonCard> fetchCards(String name, String type, String set, String id) {
        String query = buildQuery(name, type, set, id);
        return fetchCardsByQuery(query);
    }

    // Overloaded method with pagination support
    public List<PokemonCard> fetchCards(String name, String type, String set, String id, int page, int pageSize) {
        String query = buildQuery(name, type, set, id);
        return fetchCardsByQuery(query, page, pageSize);
    }

    // Original method for backward compatibility
    public List<PokemonCard> fetchCards(String nameQuery, String typeQuery) {
        return fetchCards(nameQuery, typeQuery, "", "");
    }

    // Helper method to build the API query
    private String buildQuery(String name, String type, String set, String id) {
        StringBuilder query = new StringBuilder();

        if (!name.isBlank()) appendQuery(query, "name:", name.trim());
        if (!type.isBlank()) appendQuery(query, "types:", type.trim());
        if (!set.isBlank()) appendQuery(query, "set.name:", set.trim());
        if (!id.isBlank()) appendQuery(query, "id:", id.trim());

        return query.toString();
    }

    // Helper to append query parts with proper spacing
    private void appendQuery(StringBuilder query, String prefix, String value) {
        if (query.length() > 0) query.append(" ");
        query.append(prefix).append(value);
    }

    private List<PokemonCard> fetchCardsByQuery(String query) {
        return fetchCardsByQuery(query, 1, 10);
    }

    private List<PokemonCard> fetchCardsByQuery(String query, int page, int pageSize) {
        List<PokemonCard> cards = new ArrayList<>();
        if (query.isBlank()) return cards;

        HttpURLConnection conn = null;
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String apiUrl = String.format(
                    "https://api.pokemontcg.io/v2/cards?page=%d&pageSize=%d&q=%s",
                    page, pageSize, encodedQuery
            );

            conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-Api-Key", "6c90b57d-f700-4186-bee3-997e33923403");

            if (conn.getResponseCode() == 200) {
                JsonObject response = new Gson().fromJson(
                        new InputStreamReader(conn.getInputStream()),
                        JsonObject.class
                );
                cards = parseJsonToCards(response.toString());
            } else {
                System.err.println("API request failed. HTTP Code: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching cards: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return cards;
    }

    public List<PokemonCard> parseJsonToCards(String responseBody) {
        List<PokemonCard> cards = new ArrayList<>();
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonArray data = json.getAsJsonArray("data");

            for (int i = 0; i < data.size(); i++) {
                JsonObject cardJson = data.get(i).getAsJsonObject();
                cards.add(parseSingleCard(cardJson));
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
        return cards;
    }

    private PokemonCard parseSingleCard(JsonObject cardJson) {
        String name = cardJson.has("name") ? cardJson.get("name").getAsString() : "Unknown";

        String imageUrl = "";
        if (cardJson.has("images")) {
            JsonObject images = cardJson.getAsJsonObject("images");
            imageUrl = images.has("large") ? images.get("large").getAsString() : "";
        }

        String cardType = "Unknown";
        if (cardJson.has("types")) {
            JsonArray types = cardJson.getAsJsonArray("types");
            if (types.size() > 0) {
                cardType = types.get(0).getAsString();
            }
        }

        String mechanic = "N/A";
        if (cardJson.has("subtypes")) {
            JsonArray subtypes = cardJson.getAsJsonArray("subtypes");
            if (subtypes.size() > 0) {
                mechanic = subtypes.get(0).getAsString();
            }
        }

        String moves = "";
        if (cardJson.has("attacks")) {
            JsonArray attacks = cardJson.getAsJsonArray("attacks");
            StringBuilder movesBuilder = new StringBuilder();
            attacks.forEach(attack ->
                    movesBuilder.append(attack.getAsJsonObject().get("name").getAsString()).append(", ")
            );
            moves = movesBuilder.toString().replaceAll(", $", "");
        }

        String set = "";
        if (cardJson.has("set")) {
            JsonObject setObj = cardJson.getAsJsonObject("set");
            if (setObj.has("name")) {
                set = setObj.get("name").getAsString();
            }
        }

        String cardNumber = "";
        if (cardJson.has("id")) {
            cardNumber = cardJson.get("id").getAsString();
        } else if (cardJson.has("number")) {
            cardNumber = cardJson.get("number").getAsString();
        }

        return new PokemonCard(name, imageUrl, cardType, mechanic, moves, cardNumber, set);
    }
//    public List<PokemonCard> loadCollection() {
//        List<PokemonCard> collectionCards = new ArrayList<>();
//
//        try {
//            File file = new File(COLLECTION_FILE);
//            if (file.exists()) {
//                ObjectMapper mapper = new ObjectMapper();
//                collectionCards = mapper.readValue(file,
//                        mapper.getTypeFactory().constructCollectionType(List.class, PokemonCard.class));
//            }
//        } catch (IOException e) {
//            System.err.println("Error loading collection: " + e.getMessage());
//        }
//
//        return collectionCards;
//    }
}

