package org.example.cardcollectorproject.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import org.example.cardcollectorproject.models.PokemonCard;

import java.util.ArrayList;
import java.util.List;

public class CardSearching {
    // Parse JSON response and populate the cards list
    public List<PokemonCard> parseJsonToCards(String responseBody) {
        List<PokemonCard> cards = new ArrayList<>();

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(responseBody, JsonObject.class);
        JsonArray data = json.getAsJsonArray("data");

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

        return cards;
    }
}
