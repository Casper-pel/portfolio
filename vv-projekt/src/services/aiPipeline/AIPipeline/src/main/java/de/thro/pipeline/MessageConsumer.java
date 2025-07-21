package de.thro.pipeline;

import com.rabbitmq.client.DeliverCallback;
import de.thro.shared.ConnectBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

/**
 * MessageConsumer ist ein Service, der Nachrichten von RabbitMQ konsumiert und in eine BlockingQueue legt.
 * Diese Klasse implementiert Runnable, um in einem separaten Thread zu laufen.
 */
@Service
public class MessageConsumer implements Runnable{

    private final ConnectBus connectBus;
    private final BlockingQueue<String> queue;
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    /**
     * Konstruktor, der die Abhängigkeiten injiziert.
     *
     * @param connectBus Verbindung zu RabbitMQ
     * @param queue      BlockingQueue, in die die Nachrichten gelegt werden
     */
    @Autowired
    public MessageConsumer(ConnectBus connectBus, BlockingQueue<String> queue){
        this.connectBus = connectBus;
        this.queue = queue;
    }

    /**
     * Startet den Consumer-Thread, der auf Nachrichten wartet und diese in die BlockingQueue legt.
     */
    @Override
    public void run(){
        logger.info("Starting Consumer Thread");
        try{
            Channel channel = connectBus.getChannel();
            DeliverCallback deliverCallBack = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                logger.info("Received message: {}", message);
                logger.info("test");

                try{
                    queue.put(message);
                    logger.info("Message sent to queue: {}", message);
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    logger.error("Thread got interrupted while adding to queue: {}", e.getMessage());
                }
            };
            channel.basicConsume("OfferInput", true, deliverCallBack, consumerTag -> {});
            logger.info("Waiting for message in OfferInput");
        }catch(Exception e){
            logger.error("Error in MessageConsumer", e);
        }
    }
}
