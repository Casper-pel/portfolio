package de.thro.pipeline.service;

import de.thro.pipeline.entity.Invoice;
import de.thro.pipeline.entity.Offer;
import de.thro.pipeline.repository.InvoiceRepository;
import de.thro.pipeline.repository.OfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service, der regelmäßig unbehandelte Rechnungen mit den zugehörigen Angeboten abgleicht.
 * Dieser Abgleich erfolgt als Cron-Job alle 60 Sekunden.
 */
@Service
public class MatchingService {

    private static final Logger logger = LoggerFactory.getLogger(MatchingService.class);

    private final OfferRepository offerRepository;

    private final InvoiceRepository invoiceRepository;

    private final CompareService compareService;

    /**
     * Konstruktor, der die benötigten Repositories und den CompareService injiziert.
     *
     * @param offerRepository Repository für Angebote
     * @param invoiceRepository Repository für Rechnungen
     * @param compareService Service zum Vergleichen von Angeboten und Rechnungen
     */
    public MatchingService(OfferRepository offerRepository, InvoiceRepository invoiceRepository, CompareService compareService) {
        this.offerRepository = offerRepository;
        this.invoiceRepository = invoiceRepository;
        this.compareService = compareService;
    }

    /**
     * Cron-Job, der alle 60 Sekunden ausgeführt wird, um unbehandelte Rechnungen mit den zugehörigen Angeboten abzugleichen.
     * Wenn eine Rechnung mit einem Angebot übereinstimmt, wird sie als geprüft markiert und das Ergebnis gespeichert.
     */
    @Scheduled(fixedRate = 60000)
    public void matchingJob(){
        logger.info("Starting Matching Job");

        List<Invoice> uncheckedInvoices = invoiceRepository.findByIsCheckedIsNull();

        for (Invoice invoice : uncheckedInvoices) {
            if(invoice.getOffer() == null){
                continue;
            }
            Optional<Offer> offerOpt = offerRepository.findByOfferNumber(invoice.getOffer().getOfferNumber());

            if (offerOpt.isEmpty()) {
                logger.info("No Offer found for: Invoice {}", invoice.getInvoiceId());
                continue;
            }

            Offer offer = offerOpt.get();

            compareService.compareOfferWithInvoice(offer, invoice)
                    .thenAccept(result -> {
                        if(result){
                            invoice.setIsChecked(LocalDate.now());
                        }
                        invoice.setValid(result);
                        invoiceRepository.save(invoice);
                        logger.info("Invoice {} was compared with Offer {} – Valid: {}", invoice.getInvoiceId(), offer.getOfferNumber(), result);
                    });


        }
        logger.info("Matching Cron Job ended.");
    }
}
