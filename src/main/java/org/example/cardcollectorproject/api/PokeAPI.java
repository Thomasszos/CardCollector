package org.example.cardcollectorproject.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PokeAPI {
    private static final String API_URL = "https://pokeapi.co/api/v2/pokemon-species/";

    public static String getPokemonDescription(String name) {
        try {
            // Simplify name to base species (e.g., "Flying Pikachu" → "pikachu")
            String simplified = simplifyName(name);

            String encodedName = URLEncoder.encode(simplified, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + encodedName))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                JsonArray entries = json.getAsJsonArray("flavor_text_entries");

                for (int i = 0; i < entries.size(); i++) {
                    JsonObject entry = entries.get(i).getAsJsonObject();
                    if ("en".equals(entry.getAsJsonObject("language").get("name").getAsString())) {
                        return entry.get("flavor_text").getAsString()
                                .replace('\n', ' ')
                                .replace('\f', ' ')
                                .trim();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Description not available.";
    }

    private static String simplifyName(String fullName) {
        // Remove punctuation and convert to lowercase
        String cleaned = fullName.replaceAll("[^a-zA-Z0-9\\s]", "").trim().toLowerCase();

        // Split into words
        String[] parts = cleaned.split(" ");

        // Remove suffixes like V, EX, GX, VMAX, etc.
        for (int i = parts.length - 1; i >= 0; i--) {
            String word = parts[i];
            if (!word.matches("v|ex|gx|vmax|mega|break|star|lvx|fb|g|gl|c|m|δ|δspecies")) {
                return word;
            }
        }

        // Fallback
        return cleaned;
    }
}


