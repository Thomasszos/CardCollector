package org.example.cardcollectorproject.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCollection {

    @JsonProperty("id")
    private String id;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("collectionType")
    private String collectionType; // "collection" or "watchlist"

    @JsonProperty("cards")
    private List<PokemonCard> cards;

    // Default constructor for deserialization
    public UserCollection() {
        this.cards = new ArrayList<>();
    }

    public UserCollection(String userId, String collectionType) {
        this.id = userId + "_" + collectionType;
        this.userId = userId;
        this.collectionType = collectionType;
        this.cards = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public List<PokemonCard> getCards() {
        return cards;
    }

    public void setCards(List<PokemonCard> cards) {
        this.cards = cards;
    }

    public void addCard(PokemonCard card) {
        // Check if the card already exists in the collection
        boolean cardExists = cards.stream()
                .anyMatch(c -> c.getCardNumber().equals(card.getCardNumber()));

        if (!cardExists) {
            cards.add(card);
        }
    }

    public void removeCard(String cardNumber) {
        cards.removeIf(card -> card.getCardNumber().equals(cardNumber));
    }
}
