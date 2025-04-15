
package org.example.cardcollectorproject.models;

import java.time.LocalDateTime;

public class CardPrice {

    private Long id;

    private PokemonCard pokemonCard;

    private double price;

    private LocalDateTime timestamp;

    public CardPrice() {}

    public CardPrice(PokemonCard pokemonCard, double price, LocalDateTime timestamp) {
        this.pokemonCard = pokemonCard;
        this.price = price;
        this.timestamp = timestamp;
    }

    public Long getId() {
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
}
