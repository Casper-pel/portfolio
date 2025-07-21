package de.thro.shared;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Diese Klasse stellt eine Verbindung zu RabbitMQ her und bietet Methoden zum
 * Senden von Nachrichten, Anlegen von Queues und Zugriff auf den Channel.
 *
 * Die Verbindung wird mit Parametern aus einer {@link SharedEnvConfig}-Instanz aufgebaut.
 */
public class ConnectBus {
    private static final Logger logger = LoggerFactory.getLogger(ConnectBus.class);
    private final ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private final String host;

    /**
     * Konstruktor, der eine Verbindung zu RabbitMQ mit den Konfigurationsparametern aus SharedEnvConfig aufbaut.
     *
     * @param envConfig Konfigurationseinstellungen für RabbitMQ-Verbindung
     */
    public ConnectBus(SharedEnvConfig envConfig){
        var finalFactory = new ConnectionFactory();
        this.host = envConfig.getRabbitmqHost();
        finalFactory.setHost(envConfig.getRabbitmqHost());
        finalFactory.setPort(envConfig.getRabbitmqPort());
        finalFactory.setUsername(envConfig.getRabbitmqUsername());
        finalFactory.setPassword(envConfig.getRabbitmqPassword());
        finalFactory.setConnectionTimeout(30000);
        finalFactory.setAutomaticRecoveryEnabled(true);

        factory = finalFactory;
    }

    /**
     * Konstruktor, der eine Verbindung zu RabbitMQ mit den angegebenen Host- und Port-Parametern aufbaut.
     *
     * @param host Hostname oder IP-Adresse des RabbitMQ-Servers
     * @param port Port des RabbitMQ-Servers
     */
    public void connect() throws IOException, TimeoutException{
        if(isConnected()) return;

        logger.info("Connecting to RabbitMQ at {}:{}", host, factory.getPort());
        connection = factory.newConnection();
        channel = connection.createChannel();
        logger.info("RabbitMQ connection established");
    }

    /**
     * Deklariert eine Queue mit dem angegebenen Namen.
     *
     * @param queueName Name der zu deklarierenden Queue
     * @throws IOException wenn die Verbindung oder der Channel nicht geöffnet sind
     */
    public void declareQueue(String queueName) throws IOException{
        ensureChannelOpen();
        channel.queueDeclare(queueName, true, false, false, null);
        logger.debug("Queue declared: {}", queueName);
    }

    /**
     * Sendet eine Nachricht an die angegebene Queue.
     *
     * @param queueName Name der Queue, an die die Nachricht gesendet werden soll
     * @param message   Die zu sendende Nachricht
     * @throws IOException wenn die Verbindung oder der Channel nicht geöffnet sind
     */
    public void send(String queueName, String message) throws IOException{
        ensureChannelOpen();
        channel.basicPublish("", queueName, null, message.getBytes());
        logger.debug("Message sent to {}: {}", queueName, message);
    }

    /**
     * Gibt den RabbitMQ-Channel zurück.
     *
     * @return der RabbitMQ-Channel
     * @throws IOException wenn die Verbindung oder der Channel nicht geöffnet sind
     */
    public Channel getChannel(){
        return channel;
    }

    /**
     * Schließt die RabbitMQ-Verbindung und setzt den Channel auf null.
     *
     * @throws IOException wenn ein Fehler beim Schließen der Verbindung auftritt
     */
    public void close() throws IOException{
        if(connection == null) return;
        connection.close();
        connection = null;
        logger.info("Closed RabbitMQ connection");
    }

    /**
     * Stellt sicher, dass der RabbitMQ-Channel geöffnet ist.
     *
     * @throws IOException wenn der Channel geschlossen ist
     */
    private void ensureChannelOpen() throws IOException{
        if(channel == null || !channel.isOpen()){
            throw new IOException("RabbitMQ channel is closed");
        }
    }

    /**
     * Überprüft, ob die Verbindung zu RabbitMQ geöffnet ist.
     *
     * @return true, wenn die Verbindung geöffnet ist, andernfalls false
     */
    public boolean isConnected(){
        return connection != null && connection.isOpen();
    }

    /**
     * Erstellt eine neue RabbitMQ-Queue mit dem angegebenen Namen.
     *
     * @param queueName Name der zu erstellenden Queue
     * @return eine Instanz der Queue
     * @throws IOException wenn die Verbindung oder der Channel nicht geöffnet sind
     */
    public Queue getQueue(String queueName) throws IOException {
        if (!isConnected()) {
            throw new IOException("No connection zu RabbitMQ.");
        }
        logger.info("Initialising RabbitMQ-Queue: {}", queueName);
        return new Queue(channel, queueName);
    }
}
