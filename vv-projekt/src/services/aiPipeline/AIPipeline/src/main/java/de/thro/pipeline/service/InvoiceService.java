package de.thro.pipeline.service;

import de.thro.pipeline.entity.*;
import de.thro.pipeline.modelDto.CustomerDto;
import de.thro.pipeline.modelDto.InvoiceDto;
import de.thro.pipeline.modelDto.InvoiceItemDto;
import de.thro.pipeline.modelDto.InvoiceRequestDto;
import de.thro.pipeline.repository.CustomerRepository;
import de.thro.pipeline.repository.InvoiceRepository;
import de.thro.pipeline.repository.OfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Service-Klasse für die Verarbeitung und Verwaltung von Rechnungen ({@link Invoice}).
 * Bietet Funktionen zum Abrufen und Speichern von Rechnungen, sowie zur Konvertierung
 * in DTOs für API-Kommunikation.
 */
@Service
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;

    private final CustomerRepository customerRepository;

    private final OfferRepository offerRepository;

    /**
     * Konstruktor für die InvoiceService-Klasse.
     * Initialisiert die Repositories, die für den Zugriff auf die Datenbank benötigt werden.
     *
     * @param invoiceRepository Repository für Rechnungen
     * @param customerRepository Repository für Kunden
     * @param offerRepository Repository für Angebote
     */
    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, CustomerRepository customerRepository, OfferRepository offerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.offerRepository = offerRepository;
    }

    /**
     * Ruft eine Rechnung anhand der Angebots-ID ab.
     *
     * @param OfferId die ID des Angebots, zu dem die Rechnung gehört
     * @return Optional<InvoiceDto> die Rechnung als DTO, falls gefunden, sonst leer
     */
    public Optional<InvoiceDto> getInvoiceByOfferId(String OfferId) {
        Optional<Invoice> invoice = invoiceRepository.findByOffer_OfferNumber(OfferId);
        if(invoice.isEmpty()){
            logger.info("No Invoice with OfferId {} found", OfferId);
            return Optional.empty();
        }
        return invoice.map(this::invoiceToDto);
    }

    /**
     * Ruft eine Rechnung anhand der Rechnungs-ID ab.
     *
     * @param invoiceId die ID der Rechnung
     * @return Optional<InvoiceDto> die Rechnung als DTO, falls gefunden, sonst leer
     */
    public InvoiceDto invoiceToDto(Invoice invoice) {
        if (invoice == null) {return null;}

        InvoiceDto invoiceDto = new InvoiceDto();

        invoiceDto.setInvoiceId(invoice.getInvoiceId());
        invoiceDto.setInvoiceDate(invoice.getInvoiceDate());
        invoiceDto.setInvoiceTotalSum(invoice.getInvoiceTotalSum());
        invoiceDto.setIsChecked(invoice.getIsChecked());
        invoiceDto.setValid(invoice.isValid());

        if(invoice.getOffer() != null) {
                invoiceDto.setOfferId(invoice.getOffer().getOfferNumber());
        }

        Customer customer = invoice.getCustomer();
        CustomerDto customerDto = new CustomerDto();
        customerDto.setMail(customer.getMail());
        customerDto.setPhone(customer.getPhone());
        customerDto.setCity(customer.getCity());
        customerDto.setCompanyName(customer.getCompanyName());
        customerDto.setAddressStreet(customer.getAddressStreet());
        customerDto.setPostCode(customer.getPostCode());
        customerDto.setAddressHouseNumber(customer.getAddressHouseNumber());
        invoiceDto.setCustomer(customerDto);

        List<InvoiceItemDto> invoiceItemsDto = invoice.getInvoiceItems().stream().map(item -> {
            InvoiceItemDto itemDto = new InvoiceItemDto();
            itemDto.setAmount(item.getAmount());
            itemDto.setDescription(item.getDescription());
            itemDto.setPrice(item.getPrice());
            itemDto.setPosNumber(item.getId().getPosNumber());
            return itemDto;
        }).toList();

        invoiceDto.setInvoiceItems(invoiceItemsDto);
        return invoiceDto;
    }

    /**
     * Speichert eine neue Rechnung in der Datenbank.
     * Erstellt bei Bedarf auch einen neuen Kunden, falls dieser noch nicht existiert.
     *
     * @param request die Anfrage mit den Rechnungsdaten
     * @return die ID der gespeicherten Rechnung
     */
    public Long saveInvoice(InvoiceRequestDto request) {

        Customer customer = customerRepository.findByCompanyNameAndAddressStreet(request.getCustomer().getCompanyName(), request.getCustomer().getAddressStreet())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setCompanyName(request.getCustomer().getCompanyName());
                    newCustomer.setAddressStreet(request.getCustomer().getAddressStreet());
                    newCustomer.setAddressHouseNumber(request.getCustomer().getAddressHouseNumber());
                    newCustomer.setPostCode(request.getCustomer().getPostCode());
                    newCustomer.setCity(request.getCustomer().getCity());
                    newCustomer.setPhone(request.getCustomer().getPhone());
                    newCustomer.setMail(request.getCustomer().getMail());
                    return newCustomer;
                        });

        Invoice invoice = new Invoice();
        Optional<Offer> offer = offerRepository.findById(request.getOfferId());
        offer.ifPresent(invoice::setOffer);
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setInvoiceDate(request.getInvoiceDate());
        invoice.setInvoiceTotalSum(request.getInvoiceTotalSum());
        invoice.setCustomer(customer);
        invoice.setIsChecked(null);
        invoice.setValid(false);

        Invoice tempInvoice = invoice;
        List<InvoiceItem> items = request.getInvoiceItems().stream().map(itemDto -> {
            InvoiceItem item = new InvoiceItem();
            InvoiceItemId itemId = new InvoiceItemId(null, itemDto.getPosNumber());
            item.setId(itemId);
            item.setDescription(itemDto.getDescription());
            item.setAmount(itemDto.getAmount());
            item.setPrice(itemDto.getPrice());
            item.setInvoice(tempInvoice);
            return item;
        }).toList();

        invoice.setInvoiceItems(items);
        invoice = invoiceRepository.save(invoice);

        return invoice.getInvoiceId();
    }
}
