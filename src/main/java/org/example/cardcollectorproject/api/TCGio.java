package org.example.cardcollectorproject.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import org.example.cardcollectorproject.models.PokemonCard;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public class TCGio {
    // Fetch data from the Pokémon TCG API asynchronously
    private static final String API_URL = "https://api.pokemontcg.io/v2/cards";
    private static final String API_KEY = "6c90b57d-f700-4186-bee3-997e33923403";

    public CompletableFuture<String> fetchCardData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("X-Api-Key", API_KEY)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public static CompletableFuture<Double> fetchCardPrice(String cardId) {
        String url = API_URL + "/" + cardId + "?select=id,tcgplayer";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Api-Key", API_KEY)
                .build();
        return HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    if (body == null || body.isBlank()) {
                        return 0.0;
                    }
                    try {
                        JsonObject root = new Gson().fromJson(body, JsonObject.class);
                        if (root == null || !root.has("data")) {
                            return 0.0;
                        }
                        JsonObject data = root.getAsJsonObject("data");
                        if (!data.has("tcgplayer")) {
                            return 0.0;
                        }
                        JsonObject tcg = data.getAsJsonObject("tcgplayer");
                        JsonObject prices = tcg.has("prices")
                                ? tcg.getAsJsonObject("prices")
                                : null;
                        if (prices != null) {
                            // try holofoil first
                            if (prices.has("holofoil")) {
                                JsonObject holo = prices.getAsJsonObject("holofoil");
                                if (holo.has("market")) {
                                    return holo.get("market").getAsDouble();
                                }
                            }
                            // fallback to any other variant
                            for (String key : prices.keySet()) {
                                JsonObject variant = prices.getAsJsonObject(key);
                                if (variant.has("market")) {
                                    return variant.get("market").getAsDouble();
                                }
                            }
                        }
                    } catch (Exception e) {
                        // parse error, just return 0.0
                    }
                    return 0.0;
                });
    }
    public static CompletableFuture<String> fetchCardSet(String cardId) {
        // we only need the “set” field
        String url = API_URL + "/" + cardId + "?select=id,set";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Api-Key", API_KEY)
                .build();
        return HttpClient.newHttpClient()
                .sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        JsonObject root = new Gson().fromJson(body, JsonObject.class);
                        JsonObject data = root.getAsJsonObject("data");
                        JsonObject setObj = data.getAsJsonObject("set");
                        return setObj.has("name")
                                ? setObj.get("name").getAsString()
                                : "Unknown";
                    } catch (Exception e) {
                        return "Unknown";
                    }
                });
    }
}