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




}


