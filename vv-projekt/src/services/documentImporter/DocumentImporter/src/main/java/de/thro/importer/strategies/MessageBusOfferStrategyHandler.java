package de.thro.importer.strategies;

import de.thro.shared.ConnectBus;
import de.thro.shared.SharedEnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class MessageBusOfferStrategyHandler implements IOfferStrategyHandler{
    private static final Logger logger = LoggerFactory.getLogger(MessageBusOfferStrategyHandler.class);
    private static final String QUEUE_NAME = "OfferInput";
    ConnectBus bus;

    public MessageBusOfferStrategyHandler(SharedEnvConfig envConfig){
        try{
            logger.info("current host: {}", envConfig.getRabbitmqHost());
            this.bus = new ConnectBus(envConfig);
            bus.connect();
            bus.declareQueue(QUEUE_NAME);
            logger.info("RabbitMQ connection established for strategy");
        }catch(IOException | TimeoutException e){
            throw new IllegalStateException("Failed to initialize RabbitMQ connection for queue '" + QUEUE_NAME + "' " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<Void> handle(String pdfContent){
        return CompletableFuture.runAsync(() -> {
            try{
                logger.info("Sending PDF content to messagebus");
                bus.send(QUEUE_NAME, pdfContent);
            }catch(IOException e){
                throw new MessageSendException("Message send failed", e);
            }
        });
    }

    @Override
    public void close(){
        if(bus != null){
            try{
                bus.close();
            }catch(Exception e){
                logger.error("Error closing bus connection");
            }
        }
    }
}
