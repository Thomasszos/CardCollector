package org.example.cardcollectorproject.models;

// POJO for storing card details
public class PokemonCard {
    String name;
    String imageUrl;
    String cardType;
    String mechanic; // e.g., GX, EX, V, etc.
    String moves;
    String cardNumber;
    String set;

   // Default constructor needed for Jackson deserialization
    public PokemonCard() {
        // Empty constructor required for JSON deserialization
    }

    public PokemonCard(String name, String imageUrl, String cardType, String mechanic, String moves, String cardNumber, String set) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.cardType = cardType;
        this.mechanic = mechanic;
        this.moves = moves;
        this.cardNumber = cardNumber;
        this.set = set;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getMechanic() {
        return mechanic;
    }

    public void setMechanic(String mechanic) {
        this.mechanic = mechanic;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

}