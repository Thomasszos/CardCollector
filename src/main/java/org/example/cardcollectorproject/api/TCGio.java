package org.example.cardcollectorproject.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import org.json.JSONObject;
import org.example.cardcollectorproject.models.PokemonCard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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

    public static String getCardData(String cardId) {
        try {
            URL url = new URL(API_URL + cardId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Api-Key", API_KEY);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            return content.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double extractTrendPrice(String json) {
        JSONObject obj = new JSONObject(json);
        return obj.getJSONObject("data")
                .getJSONObject("cardmarket")
                .getJSONObject("prices")
                .getDouble("trendPrice");
    }

}


