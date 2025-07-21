package de.thro.pipeline.controller;

import de.thro.pipeline.entity.Offer;
import de.thro.pipeline.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.util.List;

/**
 * REST-Controller zur Verwaltung von Angeboten.
 * Stellt Endpunkte zum Abrufen aller Angebote und Angebote eines bestimmten Kunden bereit.
 */
@Controller
@RequestMapping("/api/v1")
public class OfferController {

    private final OfferService offerService;

    /**
     * Konstruktor für OfferController.
     *
     * @param offerService Service zur Angebotsverwaltung
     */
    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Gibt eine Liste aller Angebote zurück.
     *
     * @return Liste von Offer-Objekten
     */
    @Operation(summary = "Alle Angebote abrufen", description = "Gibt eine Liste aller Angebote zurück.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Offer.class))))
    })
    @GetMapping("/offer")
    public ResponseEntity<List<Offer>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    /**
     * Gibt alle Angebote eines bestimmten Kunden zurück.
     *
     * @param customerId ID des Kunden
     * @return Liste von Offer-Objekten oder 404, falls keine gefunden wurden
     */
    @Operation(summary = "Angebote nach Kunden-ID abrufen", description = "Gibt alle Angebote eines bestimmten Kunden zurück.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Angebote gefunden",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Offer.class)))),
            @ApiResponse(responseCode = "404", description = "Keine Angebote gefunden")
    })
    @GetMapping("/offer/{customerId}")
    public ResponseEntity<List<Offer>> getOfferByCustomerId(@PathVariable Long customerId) {
        List<Offer> offers = offerService.getOfferByCustomerId(customerId);
        if (offers.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(offers);
    }

}
