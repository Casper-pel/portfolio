package de.thro.importer;

import de.thro.importer.strategies.IOfferStrategyHandler;
import de.thro.importer.strategies.LoggingOfferStrategyHandler;
import de.thro.importer.strategies.MessageBusOfferStrategyHandler;
import de.thro.shared.SharedEnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Hauptklasse für den Dokumentenimporter.
 * Initialisiert die Umgebungs-Konfiguration, startet die File-Import- und Dokumentenverarbeitungsprozesse
 * und verwaltet die Anwendungslaufzeit inklusive Shutdown-Hooks.
 */
public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);


    /**
     * Einstiegspunkt der Anwendung.
     * Initialisiert die Konfiguration und startet die Hauptprozesse.
     *
     * @param args Kommandozeilenargumente (werden aktuell nicht verwendet)
     */
    public static void main(String[] args) {

        EnvConfig envConfig = null;
        SharedEnvConfig sharedEnvConfig = null;
        try{
            envConfig = new EnvConfig();
            sharedEnvConfig = new SharedEnvConfig();
        }catch(ConfigurationException e){
            logger.error("An error occurred while loading the configuration", e);
        }

        logger.info("Initializing environment configuration...");
        if(envConfig == null){
            logger.error("Environment or Shared Env Configs are not set");
        }else{
            Runnable shutdown = startApplication(envConfig, sharedEnvConfig);

            Runtime.getRuntime().addShutdownHook(new Thread(shutdown));
        }
    }

    /**
     * Startet die File-Import- und Dokumentenverarbeitungsprozesse.
     * Initialisiert die Offer-Strategie basierend auf der Konfiguration.
     *
     * @param envConfig        Anwendungsspezifische Umgebungs-Konfiguration
     * @param sharedEnvConfig  Geteilte Umgebungs-Konfiguration (z.B. für MessageBus)
     * @return Runnable, das beim Shutdown ausgeführt wird, um die Prozesse zu stoppen
     */
    public static Runnable startApplication(EnvConfig envConfig, SharedEnvConfig sharedEnvConfig){
        if(logger.isInfoEnabled()){
            logger.info("Operating System: {}", System.getProperty("os.name"));
            logger.info("Java Version: {}", System.getProperty("java.version"));
            logger.info("Active User: {}", System.getProperty("user.name"));
        }

        logger.info("Starting Program...");

        logger.info("MAX_FILE_SIZE: {}", envConfig.getMaxFileSize());
        logger.info("PATH_OFFERS: {}", envConfig.getPathOffers());
        logger.info("LOG_LEVEL: {}", envConfig.getLogLevel());
        logger.info("current host: {}", sharedEnvConfig.getRabbitmqHost());
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

        logger.info("Starting File Importer");
        FileImport fileImport = new FileImport(envConfig, queue);
        fileImport.start();

        IOfferStrategyHandler strategy;
        // Initialize the Offer Strategy
        String strategyType = envConfig.getOfferStrategy();
        if(strategyType.equals("LoggingStrategy")) {
            strategy = new LoggingOfferStrategyHandler();
            logger.info("Logging Strategy Type: {}", strategyType);
        } else if (strategyType.equals("MessageBusStrategy")) {
            strategy = new MessageBusOfferStrategyHandler(sharedEnvConfig);
        } else {
            logger.error("Invalid offer strategy: {}", strategyType);
            throw new IllegalArgumentException("Invalid offer strategy: " + strategyType);
        }


        DocumentProcessor documentProcessor = new DocumentProcessor(queue, strategy);
        logger.info("Starting Document Processor");
        documentProcessor.start();

        return () -> {
            logger.info("Stopping File Import...");
            fileImport.stop();
            logger.info("Stopping Document Processor...");
            documentProcessor.stop();
        };
    }
}