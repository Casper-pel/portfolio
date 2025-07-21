package de.thro.persistence;

import com.rabbitmq.client.Channel;
import de.thro.shared.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Konsument für Nachrichten aus der RabbitMQ-Angebots-Warteschlange.
 * Liest verarbeitete Angebotsdaten aus der Queue und speichert sie als JSON-Dateien im Dateisystem.
 */
public class MessageConsumer implements Runnable{
    private final Channel channel;
    private final String path;
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    /**
     * Erstellt einen neuen MessageConsumer mit RabbitMQ-Queue und Persistenzpfad.
     *
     * @param queue Angebots-Queue mit RabbitMQ-Channel
     * @param path Pfad zum Persistenz-Verzeichnis
     */
    public MessageConsumer(Queue queue, String path){
        this.channel = queue.getChannel();
        this.path = path;
    }

    /**
     * Startet den Nachrichtenkonsum aus der Queue und speichert empfangene Nachrichten als Dateien.
     * Loggt Fehler und Statusmeldungen.
     */
    @Override
    public void run(){
        logger.info("Starting MessageConsumer");
        if(channel == null || !channel.isOpen()){
            logger.error("Channel is null or closed");
            return;
        }
        try{
            channel.basicConsume("ProcessedOffers", true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                logger.info("Message received");
                saveToFile(message);
            }, consumerTag -> {});
        }catch(Exception e){
            logger.error("Message consumer failed", e);
        }
    }

    /**
     * Persistiert eine empfangene Nachricht als JSON-Datei im angegebenen Verzeichnis.
     * Der Dateiname wird zufällig generiert.
     *
     * @param message Angebotsdaten als JSON-String
     */
    public void saveToFile(String message){
        try{
            String fileName = "offer_" + UUID.randomUUID() + ".json";
            Path finalPath = Paths.get(path, fileName);
            logger.info("Path: {}", finalPath);
            Files.writeString(finalPath, message);
            logger.info("File saved to: {}", finalPath);
        }catch(Exception e){
            logger.error("Failed to save file:", e);
        }
    }
}

