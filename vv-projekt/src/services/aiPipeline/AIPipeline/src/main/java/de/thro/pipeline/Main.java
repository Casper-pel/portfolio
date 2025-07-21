package de.thro.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.thro.pipeline.config.ConnectBusConfig;
import de.thro.pipeline.repository.CustomerRepository;
import de.thro.pipeline.repository.OfferItemRepository;
import de.thro.pipeline.repository.OfferRepository;
import de.thro.shared.ConnectBus;
import de.thro.shared.RabbitMQConfig;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main-Klasse für den AI-Pipeline-Service.
 * Diese Klasse startet die Spring Boot Anwendung und initialisiert den AI-Pipeline-Service.
 */
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(RabbitMQConfig.class)
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String QUEUE_INPUT = "OfferInput";
    private static final String QUEUE_PROCESSED = "ProcessedOffers";


    private final ConnectBusConfig connectBusConfig;

    private OfferProcessor offerProcessor;

    private final OfferRepository offerRepository;

    private final CustomerRepository customerRepository;

    private final OfferItemRepository offerItemRepository;

    /**
     * Konstruktor, der die benötigten Repositories und den ConnectBusConfig injiziert.
     *
     * @param connectBusConfig Konfiguration für den ConnectBus
     * @param offerProcessor   Verarbeitet Angebote
     * @param offerRepository  Repository für Angebote
     * @param customerRepository Repository für Kunden
     * @param offerItemRepository Repository für Angebotsposten
     */
@Autowired
public Main(ConnectBusConfig connectBusConfig, OfferProcessor offerProcessor,
                 OfferRepository offerRepository, CustomerRepository customerRepository,
                 OfferItemRepository offerItemRepository) {
        this.connectBusConfig = connectBusConfig;
        this.offerProcessor = offerProcessor;
        this.offerRepository = offerRepository;
        this.customerRepository = customerRepository;
        this.offerItemRepository = offerItemRepository;
    }

    /**
     * Startet die Spring Boot Anwendung.
     *
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args){
        SpringApplication.run(Main.class, args);
    }

    /**
     * Initialisiert den AI-Pipeline-Service, verbindet sich mit RabbitMQ und startet die Verarbeitung von Angeboten.
     */
    @PostConstruct
    public void startService(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();

        logger.info("AI pipeline service starting...");

        ConnectBus bus = connectBusConfig.createConnectBus();

        offerProcessor = new OfferProcessor(offerItemRepository ,bus, blockingQueue, customerRepository, offerRepository);

        try{
            bus.connect();
            bus.declareQueue(QUEUE_INPUT);
            bus.declareQueue(QUEUE_PROCESSED);

            Thread conumerThread = new Thread(new MessageConsumer(bus, blockingQueue));
            Thread processorThread = new Thread(offerProcessor);

            conumerThread.start();
            processorThread.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shuttind down AI pipeline service");

                offerProcessor.stop();

                try {
                    bus.close();
                } catch (IOException e) {
                    logger.error("Failed to close RabbitMQ connection during shutdown: {}", e.getMessage());
                }

                conumerThread.interrupt();
                processorThread.interrupt();
            }));
        }catch(Exception e){
            logger.error("Error starting AI pipeline service", e);
        }
    }
}