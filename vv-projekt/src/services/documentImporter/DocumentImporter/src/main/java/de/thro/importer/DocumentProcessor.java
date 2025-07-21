package de.thro.importer;


import de.thro.importer.strategies.IOfferStrategyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;

/**
 * Verarbeitet Dokumente aus einer Warteschlange mithilfe einer Offer-Strategie.
 * Läuft in einem eigenen Thread und verarbeitet JSON-Dokumente asynchron.
 */
public class DocumentProcessor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    private final BlockingQueue<String> queue;
    private final IOfferStrategyHandler strategyHandler;
    private volatile boolean running = false;
    private Thread thread;

    /**
     * Erstellt einen neuen DocumentProcessor mit gegebener Warteschlange und Strategie-Handler.
     *
     * @param queue Warteschlange mit Dokumenten (als JSON-Strings)
     * @param strategyHandler Handler zur Verarbeitung der Dokumente
     */
    public DocumentProcessor(BlockingQueue<String> queue, IOfferStrategyHandler strategyHandler){
        this.queue = queue;
        this.strategyHandler = strategyHandler;
    }

    /**
     * Startet die Verarbeitung der Dokumente in einem eigenen Thread.
     */
    public void start(){
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Stoppt die Verarbeitung und unterbricht den Thread.
     */
    public void stop(){
        running = false;
        if(thread != null){
            thread.interrupt();
        }
    }


    /**
     * Hauptverarbeitungsschleife, die Dokumente aus der Warteschlange entnimmt und verarbeitet.
     * Beendet sich bei Unterbrechung oder Fehler.
     */
    @Override
    public void run() {
        logger.info("Document Processing started");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                String jsonString = queue.take();

                strategyHandler.handle(jsonString)
                        .exceptionally(e -> {
                            logger.error("Failed to process document: {}", e.getMessage());
                            return null;
                        });
            } catch (InterruptedException e) {
                logger.error("Processing interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Unexpected error: {}", e.getMessage());
            }
        }
    }
}
