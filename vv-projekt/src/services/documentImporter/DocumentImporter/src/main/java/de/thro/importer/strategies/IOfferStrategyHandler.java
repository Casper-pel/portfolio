package de.thro.importer.strategies;

import java.util.concurrent.CompletableFuture;

public interface IOfferStrategyHandler extends AutoCloseable{

    CompletableFuture<Void> handle(String pdfContent);
}
