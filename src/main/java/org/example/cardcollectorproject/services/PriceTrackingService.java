
package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.models.CardPrice;
import org.example.cardcollectorproject.models.PokemonCard;

import java.time.LocalDateTime;
import java.util.List;

public class PriceTrackingService {

    private final CardPriceRepository cardPriceRepository;

    public PriceTrackingService(CardPriceRepository cardPriceRepository) {
        this.cardPriceRepository = cardPriceRepository;
    }

    public void savePriceForCard(PokemonCard card, double price) {
        CardPrice cardPrice = new CardPrice(card, price, LocalDateTime.now());
        cardPriceRepository.save(cardPrice);
    }

    public List<CardPrice> getPriceHistory(PokemonCard card) {
        return cardPriceRepository.findByPokemonCard(card);
    }
}
