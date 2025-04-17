package org.example.cardcollectorproject.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.cardcollectorproject.models.PokemonCard;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CardSearching {

    public List<PokemonCard> fetchCards(String nameQuery, String typeQuery) {
        List<PokemonCard> cards = new ArrayList<>();
        try {
            String query = "";
            if (nameQuery != null && !nameQuery.isBlank()) {
                query += "name:" + nameQuery.trim();
            }
            if (typeQuery != null && !typeQuery.isBlank()) {
                if (!query.isEmpty()) query += " ";
                query += "types:" + typeQuery.trim();
            }

            // If no query is given, return nothing (don't pull all cards)
            if (query.isBlank()) {
                return cards;
            }

            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String apiUrl = "https://api.pokemontcg.io/v2/cards?q=" + encodedQuery;

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                Gson gson = new Gson();
                JsonObject response = gson.fromJson(new InputStreamReader(conn.getInputStream()), JsonObject.class);
                JsonArray data = response.getAsJsonArray("data");

                for (int i = 0; i < data.size(); i++) {
                    JsonObject cardJson = data.get(i).getAsJsonObject();

                    String name = cardJson.has("name") ? cardJson.get("name").getAsString() : "Unknown";

                    String imageUrl = "";
                    if (cardJson.has("images")) {
                        JsonObject images = cardJson.getAsJsonObject("images");
                        imageUrl = images.has("large") ? images.get("large").getAsString() : "";
                    }

                    String cardType = cardJson.has("types") ? cardJson.get("types").getAsJsonArray().toString() : "";
                    String mechanic = cardJson.has("subtypes") ? cardJson.get("subtypes").getAsJsonArray().toString() : "N/A";

                    String moves = "";
                    if (cardJson.has("attacks")) {
                        JsonArray attacks = cardJson.getAsJsonArray("attacks");
                        StringBuilder movesBuilder = new StringBuilder();
                        attacks.forEach(attack -> movesBuilder.append(attack.getAsJsonObject().get("name").getAsString()).append(" "));
                        moves = movesBuilder.toString().trim();
                    }

                    String cardNumber = cardJson.has("number") ? cardJson.get("number").getAsString() : "";

                    cards.add(new PokemonCard(name, imageUrl, cardType, mechanic, moves, cardNumber));
                }
            } else {
                System.err.println("Failed to fetch cards: HTTP " + conn.getResponseCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return cards;
    }
}

