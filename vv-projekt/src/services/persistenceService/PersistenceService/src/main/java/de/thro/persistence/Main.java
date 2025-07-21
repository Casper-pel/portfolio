package de.thro.persistence;

import de.thro.shared.ConnectBus;
import de.thro.shared.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Einstiegspunkt für den Persistence-Service.
 * Initialisiert die Umgebungs-Konfiguration, startet den Nachrichtenkonsumenten und verwaltet das Herunterfahren.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Hauptmethode des Persistence-Service.
     * Initialisiert die Konfiguration, startet den Service und registriert einen Shutdown-Hook.
     *
     * @param args Kommandozeilenargumente (werden nicht verwendet)
     */
    public static void main(String[] args){

        EnvConfig envConfig = new EnvConfig();

        Runnable shutdown = start(envConfig);

        Runtime.getRuntime().addShutdownHook(new Thread(shutdown));

    }

    /**
     * Startet den Persistence-Service.
     * Stellt die Verbindung zum Bus her, startet den MessageConsumer und gibt eine Shutdown-Routine zurück.
     *
     * @param envConfig Umgebungs-Konfiguration
     * @return Runnable für das Herunterfahren des Service
     */
    public static Runnable start(EnvConfig envConfig){
        String path = envConfig.getPersistencePath();
        logger.info("Persistence service starting...");


        ConnectBus connectBus = new ConnectBus(envConfig);

        try {
            connectBus.connect();


            Queue consumerQueue = connectBus.getQueue("ProcessedOffers");
            Thread consumerThread = new Thread(new MessageConsumer(consumerQueue, path));
            consumerThread.setUncaughtExceptionHandler((t, e) -> logger.error("Exception in consumer thread", e));
            consumerThread.start();


        } catch (Exception e) {
            logger.error("Error starting persistence service", e);
            System.exit(1);
        }

        return () -> logger.info("Shutting down PersistenceService...");
    }
}