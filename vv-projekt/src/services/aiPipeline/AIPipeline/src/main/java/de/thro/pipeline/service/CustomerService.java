package de.thro.pipeline.service;

import de.thro.pipeline.entity.Customer;
import de.thro.pipeline.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;


/**
 * Service-Klasse für die Verwaltung von {@link Customer}-Entitäten.
 * Bietet Methoden zur Abfrage aller Kunden oder eines bestimmten Kunden anhand der ID.
 */
@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    /**
     * Konstruktor für {@code CustomerService}.
     *
     * @param customerRepository Repository zur Datenbankabfrage von Kunden.
     */
    public CustomerService(CustomerRepository customerRepository){
        this.customerRepository = customerRepository;
    }

    /**
     * Liefert eine Liste aller Kunden.
     *
     * @return Liste aller {@link Customer}-Entitäten.
     */
    public List<Customer> getAllCustomers(){
        logger.info("getting all customers");
        return customerRepository.findAll();
    }

    /**
     * Liefert einen Kunden anhand der angegebenen ID.
     *
     * @param customerId ID des gesuchten Kunden.
     * @return {@link Customer}-Entität, wenn gefunden, sonst {@code null}.
     */
    public Customer getCustomer(@PathVariable Long customerId){
        Optional<Customer> customer = customerRepository.findById(customerId);
        if(customer.isPresent()){
            return customer.get();
        }
        logger.info("customer with id: {} not found",  customerId);
        return null;
    }

}
