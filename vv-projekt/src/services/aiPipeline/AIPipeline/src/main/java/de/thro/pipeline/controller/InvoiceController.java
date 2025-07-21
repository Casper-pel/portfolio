package de.thro.pipeline.controller;

import de.thro.pipeline.modelDto.InvoiceDto;
import de.thro.pipeline.modelDto.InvoiceRequestDto;
import de.thro.pipeline.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

/**
 * REST-Controller zur Verwaltung von Rechnungen.
 * Stellt Endpunkte zum Abrufen und Erstellen von Rechnungen bereit.
 */
@Controller
@RequestMapping("/api/v1")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceService invoiceService;

    /**
     * Konstruktor für InvoiceController.
     *
     * @param invoiceService Service zur Rechnungsverwaltung
     */
    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    /**
     * Gibt eine Rechnung anhand der Offer-ID zurück.
     *
     * @param offerId Angebots-ID
     * @return InvoiceDto-Objekt oder 404, falls nicht gefunden
     */
    @Operation(summary = "Rechnung nach Offer-ID abrufen", description = "Gibt eine Rechnung anhand der Offer-ID zurück.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rechnung gefunden",
                    content = @Content(schema = @Schema(implementation = InvoiceDto.class))),
            @ApiResponse(responseCode = "404", description = "Rechnung nicht gefunden")
    })
    @GetMapping("/invoice/{offerId}")
    public ResponseEntity<InvoiceDto> getInvoiceByOfferId(@PathVariable String offerId){
        Optional<InvoiceDto> invoiceDto = invoiceService.getInvoiceByOfferId(offerId);
        if (invoiceDto.isEmpty()) {
            logger.info("No Invoice with offerId {} found", offerId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoiceDto.get());
    }

    /**
     * Erstellt eine neue Rechnung.
     *
     * @param request InvoiceRequestDto mit Rechnungsdaten
     * @return ID der erstellten Rechnung oder 400 bei Fehler
     */
    @Operation(summary = "Neue Rechnung erstellen", description = "Erstellt eine neue Rechnung und gibt deren ID zurück.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rechnung erfolgreich erstellt",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrage")
    })
    @PostMapping("/invoice")
    public ResponseEntity<Long> createInvoice(@RequestBody InvoiceRequestDto request){
        Long invoiceId = invoiceService.saveInvoice(request);
        if(invoiceId == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(invoiceId);
    }
}
