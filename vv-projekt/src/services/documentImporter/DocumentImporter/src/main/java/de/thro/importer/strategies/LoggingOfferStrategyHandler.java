package de.thro.importer.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class LoggingOfferStrategyHandler implements IOfferStrategyHandler{
    private static final Logger logger = LoggerFactory.getLogger(LoggingOfferStrategyHandler.class);

    @Override
    public CompletableFuture<Void> handle(String pdfContent){
        return CompletableFuture.runAsync(() -> {
            logger.info("PDF content processed: {}", pdfContent);
            logger.info("PDF content processed via logging strategy");
        });
    }

    @Override
    public void close(){
    }
}
