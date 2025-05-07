package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.models.CardPrice;
import org.example.cardcollectorproject.models.PokemonCard;
import java.util.List;

public class CosmosCardPriceRepository implements CardPriceRepository {
    private final CosmosDbService cosmosDbService;

    public CosmosCardPriceRepository(CosmosDbService cosmosDbService) {
        this.cosmosDbService = cosmosDbService;
    }

    @Override
    public void save(CardPrice price) {
        cosmosDbService.saveCardPrice(price);
    }

    @Override
    public List<CardPrice> findByPokemonCard(PokemonCard card) {
        return cosmosDbService.getCardPriceHistory(card.getCardNumber());
    }
} 