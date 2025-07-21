package de.thro.pipeline.service;

import de.thro.pipeline.entity.Invoice;
import de.thro.pipeline.entity.Offer;
import de.thro.pipeline.proxy.AIProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for comparing an {@link Offer} with an {@link Invoice} using an AI-based comparison mechanism.
 * This class uses an injected {@link AIProxy} to evaluate the similarity or match between the two entities.
 */
@Service
public class CompareService {

    private static final Logger logger = LoggerFactory.getLogger(CompareService.class);
    private final AIProxy aiProxy;

    /**
     * Constructs a new {@code CompareService} with the specified {@link AIProxy}.
     *
     * @param aiProxy the AI proxy used to perform the comparison.
     */
    @Autowired
    public CompareService(AIProxy aiProxy) {
        this.aiProxy = aiProxy;
    }

    /**
     * Asynchronously compares the given {@link Offer} and {@link Invoice} using AI.
     * The comparison checks for equality of key fields like offer number, date, value, and customer ID.
     *
     * @param offer   the offer to be compared.
     * @param invoice the invoice to be compared.
     * @return a {@link CompletableFuture} containing {@code true} if the offer and invoice match, {@code false} otherwise.
     */
    @Async
    public CompletableFuture<Boolean> compareOfferWithInvoice(Offer offer, Invoice invoice) {
        logger.info("Starting comparison for Offer[{}] and Invoice[{}]", offer.getOfferNumber(), invoice.getInvoiceNumber());

        if(!isValid(offer) || !isValid(invoice)) {
            logger.warn("Invalid Offer[{}] or Invoice[{}]: required fields missing or incorrect", offer.getOfferNumber(), invoice.getInvoiceNumber());
            return CompletableFuture.completedFuture(false);
        }

        try {
            String prompt = generateComparisonPrompt(offer, invoice);
            logger.info("Prompt created");
            String aiResponse = aiProxy.executeAIRequest(prompt);
            logger.info("AI Response[{}]", aiResponse);
            boolean isValid = parseAiResult(aiResponse);
            logger.info("AI comparison result for Offer[{}] and Invoice[{}]: {}", offer.getOfferNumber(), invoice.getInvoiceNumber(), isValid);
            return CompletableFuture.completedFuture(isValid);
        }catch(Exception e){
            logger.error("Error during AI comparison for Offer[{}] and Invoice[{}]: {}", offer.getOfferNumber(), invoice.getInvoiceNumber(), e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Generates the prompt used to compare the offer and invoice via AI.
     *
     * @param offer   the offer entity.
     * @param invoice the invoice entity.
     * @return the prompt string to send to the AI.
     */
    private String generateComparisonPrompt(Offer offer, Invoice invoice) {
        logger.info("{} {}", offer, invoice);
        return String.format("""
            Compare the given offer with the corresponding Invoice.
            Check if the %s = %s, %s = %s, %s = %s & %s = %s
            Only return 'true', when their contents match:
            
            please only answer with "true" or "false"
            """,offer.getOfferNumber(), invoice.getOffer().getOfferNumber(), offer.getOfferDate().toString(), invoice.getInvoiceDate().toString(), offer.getOfferValue().toString(), invoice.getInvoiceTotalSum().toString(), offer.getCustomer().getId().toString(), invoice.getCustomer().getId().toString());
    }

    /**
     * Parses the response from the AI service.
     *
     * @param aiResponse the AI's response string.
     * @return {@code true} if the response is "true", {@code false} otherwise.
     */
    private boolean parseAiResult(String aiResponse) {
        return aiResponse.trim().equals("true");
    }

    /**
     * Validates the given {@link Offer} for required fields.
     *
     * @param offer the offer to validate.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    private boolean isValid(Offer offer){
        return offer != null && offer.getOfferNumber() != null && offer.getOfferValue() != null;
    }

    /**
     * Validates the given {@link Invoice} for required fields.
     *
     * @param invoice the invoice to validate.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    private boolean isValid(Invoice invoice){
        return invoice != null && invoice.getInvoiceNumber() != null && invoice.getInvoiceTotalSum() != null;
    }
}
