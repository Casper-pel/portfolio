package de.thro.shared;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Diese Klasse repräsentiert eine RabbitMQ-Queue und bietet Methoden zum Senden von Nachrichten.
 * Sie stellt sicher, dass die Queue korrekt initialisiert und der Channel offen ist, bevor Nachrichten gesendet werden.
 */
public class Queue {
    private static final Logger logger = LoggerFactory.getLogger(Queue.class);
    private Channel channel;
    private String queueName;

    /**
     * Standardkonstruktor, der eine leere Queue-Instanz erstellt.
     * Wird verwendet, wenn die Queue später initialisiert wird.
     */
    public Queue(){

    }

    /**
     * Konstruktor, der eine RabbitMQ-Queue mit dem angegebenen Channel und Queue-Namen initialisiert.
     *
     * @param channel   Der RabbitMQ-Kanal, der für die Kommunikation verwendet wird.
     * @param queueName Der Name der Queue, die erstellt oder verwendet werden soll.
     * @throws IOException Wenn ein Fehler beim Erstellen der Queue auftritt.
     */
    public Queue(Channel channel, String queueName) throws IOException {
        this.channel = channel;
        this.queueName = queueName;
        initializeQueue();
        logger.info("Queue initialized: {}", queueName);
    }

    /**
     * Konstruktor, der eine RabbitMQ-Queue mit dem angegebenen Channel und Queue-Namen initialisiert.
     * Der Channel wird aus der SharedEnvConfig abgerufen.
     *
     * @param queueName Der Name der Queue, die erstellt oder verwendet werden soll.
     * @throws IOException Wenn ein Fehler beim Erstellen der Queue auftritt.
     */
    private void initializeQueue() throws IOException {
        ensureChannelOpen();
        channel.queueDeclare(queueName, true, false, false, null);
        channel.confirmSelect();
    }

    /**
     * Sendet eine Nachricht an die RabbitMQ-Queue.
     *
     * @param message Die zu sendende Nachricht.
     * @throws IOException Wenn ein Fehler beim Senden der Nachricht auftritt.
     */
    private void ensureChannelOpen() throws IOException{
        if(channel == null || !channel.isOpen()){
            throw new IOException("RabbitMQ channel is closed");
        }
    }

    public void sendMessage(String message) throws IOException {
        ensureChannelOpen();
        channel.basicPublish("", queueName, null, message.getBytes());
        logger.info("Message sent to queue {}: {}", queueName, message);
    }
    public void close() throws IOException, TimeoutException {
        channel.close();
    }

    /**
     * Gibt den Namen der Queue zurück.
     *
     * @return Der Name der Queue.
     */
    public Channel getChannel() {
        return channel;
    }


}
