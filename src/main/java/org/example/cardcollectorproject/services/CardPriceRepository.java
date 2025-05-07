package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.models.CardPrice;
import org.example.cardcollectorproject.models.PokemonCard;

import java.util.List;

/**
 * Repository for CardPrice objects. For Cosmos DB, the partition key is 'cardprice'.
 */
public interface CardPriceRepository {
    void save(CardPrice price);
    List<CardPrice> findByPokemonCard(PokemonCard card);
}
