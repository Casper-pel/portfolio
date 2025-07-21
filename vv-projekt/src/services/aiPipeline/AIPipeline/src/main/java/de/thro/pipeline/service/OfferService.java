package de.thro.pipeline.service;

import de.thro.pipeline.entity.Offer;
import de.thro.pipeline.repository.OfferRepository;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service-Klasse für die Verwaltung von Angeboten.
 * Diese Klasse enthält Methoden zum Abrufen von Angeboten aus der Datenbank.
 */
@Service
public class OfferService {

    private static final Logger logger = LoggerFactory.getLogger(OfferService.class);

    private final OfferRepository offerRepository;

    /**
     * Konstruktor, der das OfferRepository injiziert.
     *
     * @param offerRepository das Repository für Angebote
     */
    @Autowired
    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public List<Offer> getAllOffers(){
        List<Offer> offers = offerRepository.findAll();
        if(offers.isEmpty()){
            logger.info("No Offers found");
        }
        return offers;
    }

    /**
     * Liefert ein Angebot anhand der ID.
     *
     * @param id die ID des Angebots
     * @return Optional mit dem Angebot, falls gefunden, sonst leer
     */
    public List<Offer> getOfferByCustomerId(Long customerId){
        List<Offer> offer = offerRepository.findByCustomerId(customerId);

        if(!offer.isEmpty()){
            return offer;
        }
        logger.info("No Offers with customerId {} found", customerId);
        return Collections.emptyList();
    }
}
