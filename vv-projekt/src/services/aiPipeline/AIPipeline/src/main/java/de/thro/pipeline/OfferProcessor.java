package de.thro.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.thro.pipeline.entity.*;
import de.thro.pipeline.modelDto.CustomerDto;
import de.thro.pipeline.modelDto.OfferDto;
import de.thro.pipeline.modelDto.OfferItemDto;
import de.thro.pipeline.repository.CustomerRepository;
import de.thro.pipeline.repository.OfferItemRepository;
import de.thro.pipeline.repository.OfferRepository;
import de.thro.shared.ConnectBus;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Service, der Angebote verarbeitet, die über eine BlockingQueue empfangen werden.
 * Diese Klasse implementiert Runnable, um in einem separaten Thread zu laufen.
 * Sie kommuniziert mit anderen Komponenten über den ConnectBus.
 */
@Service
public class OfferProcessor implements Runnable{

    private final ObjectMapper objectMapper;
    private final OfferItemRepository offerItemRepository;
    private final CustomerRepository customerRepository;
    private final OfferRepository offerRepository;
    private volatile boolean running = true;
    private final ConnectBus connectBus;
    private final BlockingQueue<String> queue;
    private static final Logger logger = LoggerFactory.getLogger(OfferProcessor.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Konstruktor, der die benötigten Repositories und den ConnectBus injiziert.
     *
     * @param offerItemRepository Repository für Angebotsposten
     * @param connectBus          ConnectBus für die Kommunikation
     * @param queue               BlockingQueue für die Verarbeitung von Angeboten
     * @param customerRepository  Repository für Kunden
     * @param offerRepository     Repository für Angebote
     */
    @Autowired
    public OfferProcessor(OfferItemRepository offerItemRepository, ConnectBus connectBus,
                          BlockingQueue<String> queue, CustomerRepository customerRepository, OfferRepository offerRepository)
  {
      this.objectMapper = new ObjectMapper();
      this.objectMapper.registerModule(new JavaTimeModule());
      this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // optional
      this.offerItemRepository = offerItemRepository;
      this.connectBus = connectBus;
      this.queue = queue;
      this.customerRepository = customerRepository;
      this.offerRepository = offerRepository;
  }

    /**
     * Startet den OfferProcessor in einem neuen Thread.
     */
    @Override
    public void run(){
        while(running){
            try{
                String input = queue.take();
                logger.info("Processing offer: {}", input);
                String output = processOffer(input);
                connectBus.send("ProcessedOffers", output);
            }catch(InterruptedException e){
                logger.info("Processor interrupted due to shutdown");
                Thread.currentThread().interrupt();
                break;
            }catch(Exception e){
                logger.error("Error occurred while Processing offer", e);
            }
        }
        logger.info("Offer Processor terminated");
    }

    /**
     * Verarbeitet ein Angebot, das als JSON-String übergeben wird.
     * Extrahiert die relevanten Informationen, speichert sie in der Datenbank und gibt das Angebot als JSON-String zurück.
     *
     * @param input JSON-String des Angebots
     * @return JSON-String des verarbeiteten Angebots
     */
    private String processOffer(String input){
        try {
            JsonNode json = objectMapper.readTree(input);
            String content = getValue(json, "content");
            JsonNode jsonContent = objectMapper.readTree(content);

            CustomerDto customerDto = new CustomerDto();
            customerDto.setCompanyName(getValue(jsonContent, "companyName"));
            customerDto.setAddressStreet(getValue(jsonContent, "addressStreet"));
            customerDto.setAddressHouseNumber(getValue(jsonContent, "addressHouseNumber"));
            customerDto.setPostCode(getValue(jsonContent, "postCode"));
            customerDto.setCity(getValue(jsonContent, "city"));
            customerDto.setPhone(getValue(jsonContent, "phone"));
            customerDto.setMail(getValue(jsonContent, "mail"));

            OfferDto offerDto = new OfferDto();
            offerDto.setOfferNumber(getValue(jsonContent, "offerNumber"));
            String offerDate = getValue(jsonContent, "offerDate");
            offerDto.setOfferDate(LocalDate.parse(offerDate, DATE_FORMAT));
            String offerValidTill = getValue(jsonContent, "validTillDate");
            offerDto.setOfferValidTill(LocalDate.parse(offerValidTill, DATE_FORMAT));
            String totalPrice = getValue(jsonContent, "totalPrice");
            offerDto.setOfferValue(new BigDecimal(totalPrice));

            ArrayNode items = (ArrayNode) jsonContent.get("invoiceItems");
            List<OfferItemDto> itemDtos = new ArrayList<>();
            for(JsonNode item : items){
                OfferItemDto offerItemDto = new OfferItemDto();
                offerItemDto.setPosNumber(Integer.parseInt(getValue(item, "posNumber")));
                offerItemDto.setDescription(getValue(item, "description"));
                offerItemDto.setAmount(Integer.parseInt(getValue(item, "amount")));
                offerItemDto.setPrice(new BigDecimal(getValue(item, "price")));
                itemDtos.add(offerItemDto);
            }

            offerDto.setOfferItemsDto(itemDtos);
            offerDto.setCustomerDto(customerDto);

            Customer customer = customerRepository.findByCompanyNameAndAddressStreet(customerDto.getCompanyName(), customerDto.getAddressStreet()).orElseGet(() -> {
                Customer newCustomer = new Customer();
                newCustomer.setCompanyName(customerDto.getCompanyName());
                newCustomer.setAddressStreet(customerDto.getAddressStreet());
                newCustomer.setAddressHouseNumber(customerDto.getAddressHouseNumber());
                newCustomer.setPostCode(customerDto.getPostCode());
                newCustomer.setCity(customerDto.getCity());
                newCustomer.setPhone(customerDto.getPhone());
                newCustomer.setMail(customerDto.getMail());
                return customerRepository.save(newCustomer);
            });

            if (customer.getId() == null) {
                logger.error("Customer ID is null -> Customer not saved correctly");
                throw new RuntimeException("Customer ID is null");
            }
            logger.info("Customer found or created: {}", customer.getId());

            Offer offer = new Offer();
            offer.setOfferNumber(offerDto.getOfferNumber());
            offer.setOfferDate(offerDto.getOfferDate());
            offer.setOfferValidTill(offerDto.getOfferValidTill());
            offer.setOfferValue(offerDto.getOfferValue());
            offer.setCustomer(customer);
            offer = offerRepository.save(offer);
            logger.info("Offer saved to database: {}", offer);

            Offer fixedOffer = offer;
            offerDto.getOfferItemsDto().forEach((itemDto) -> {
                OfferItemId offerItemId = new OfferItemId(fixedOffer.getOfferNumber(), itemDto.getPosNumber());
                OfferItem offerItem = new OfferItem();
                offerItem.setId(offerItemId);
                offerItem.setDescription(itemDto.getDescription());
                offerItem.setAmount(itemDto.getAmount());
                offerItem.setPrice(itemDto.getPrice());
                offerItem.setOffer(fixedOffer);
                offerItemRepository.save(offerItem);
            });

            logger.info("Offer saved to database: {}", objectMapper.writeValueAsString(offerDto));
            return objectMapper.writeValueAsString(offerDto);
        }catch(Exception e){
            logger.error("Error occurred while processing offer", e);
            throw new RuntimeException("Error occurred while processing offer", e);
        }
    }

    /**
     * Hilfsmethode, um den Wert eines bestimmten Schlüssels aus einem JsonNode zu extrahieren.
     * Gibt einen leeren String zurück, wenn der Schlüssel nicht vorhanden ist oder der Wert leer ist.
     *
     * @param node JsonNode, aus dem der Wert extrahiert werden soll
     * @param key  Schlüssel, dessen Wert extrahiert werden soll
     * @return Wert des Schlüssels als String oder leerer String, wenn nicht vorhanden
     */
    private String getValue(JsonNode node, String key) {
        String value = node.path(key).asText("").trim();
        return value.isEmpty() ? "" : value;
    }

    /**
     * Stoppt den OfferProcessor und setzt das Flag running auf false.
     * Diese Methode wird aufgerufen, wenn der Service heruntergefahren wird.
     */
    @PreDestroy
    public void stop(){
        running = false;
        logger.info("OfferProcessor shutting down.");
    }
}
