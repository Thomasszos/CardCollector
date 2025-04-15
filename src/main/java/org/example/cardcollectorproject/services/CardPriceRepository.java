
package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.models.CardPrice;
import org.example.cardcollectorproject.models.PokemonCard;

import java.util.List;

public interface CardPriceRepository {
    void save(CardPrice price);
    List<CardPrice> findByPokemonCard(PokemonCard card);
}
