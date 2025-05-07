package org.example.cardcollectorproject.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardPrice {

    @JsonProperty("id")
    private String id;

    @JsonProperty("pokemonCard")
    private PokemonCard pokemonCard;

    @JsonProperty("price")
    private double price;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("cardprice")
    private String cardprice;

    public CardPrice() {}

    public CardPrice(PokemonCard pokemonCard, double price, LocalDateTime timestamp) {
        this.pokemonCard = pokemonCard;
        this.price = price;
        this.timestamp = timestamp;
        this.cardprice = pokemonCard.getCardNumber();
    }

    public String getId() {
        return id;
    }

    public PokemonCard getPokemonCard() {
        return pokemonCard;
    }

    public void setPokemonCard(PokemonCard pokemonCard) {
        this.pokemonCard = pokemonCard;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setId(String id) { this.id = id; }

    public String getCardprice() { return cardprice; }

    public void setCardprice(String cardprice) { this.cardprice = cardprice; }
}
