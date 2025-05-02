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
import java.util.Random;

public class PokeAPI {
    private static final String API_URL = "https://pokeapi.co/api/v2/pokemon-species/";

    public static String getPokemonDescription(String name) {
        try {
            // Sanitize name for URI
            String encodedName = URLEncoder.encode(name.toLowerCase().split(" ")[0], StandardCharsets.UTF_8);

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

        // Return Minecraft-style enchantment text if no description is found
        return generateEnchantmentText();
    }

    private static String generateEnchantmentText() {
        String symbols = "ᔑʖᓵ↸ᒷ⎓⊣⍑╎⋮ꖌꖎᒲリ𝙹!¡ᑑ∷ᓭℸ ̣⚍⍊∴ ̇/||𝘿";
        Random random = new Random();
        StringBuilder enchantment = new StringBuilder();
        int length = 50 + random.nextInt(30); // random length between 50–80

        for (int i = 0; i < length; i++) {
            enchantment.append(symbols.charAt(random.nextInt(symbols.length())));
        }
        return enchantment.toString();
    }
}



