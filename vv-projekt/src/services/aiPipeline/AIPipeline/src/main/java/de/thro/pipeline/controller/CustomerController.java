package de.thro.pipeline.controller;

import de.thro.pipeline.entity.Customer;
import de.thro.pipeline.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.util.List;

/**
 * REST-Controller zur Verwaltung von Kunden.
 * Stellt Endpunkte zum Abrufen aller Kunden und eines einzelnen Kunden bereit.
 */
@RestController
@RequestMapping("/api/v1/")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    /**
     * Konstruktor für CustomerController.
     *
     * @param customerService Service zur Kundenverwaltung
     */
    @Autowired
    public CustomerController(CustomerService customerService){
        this.customerService = customerService;
    }

    /**
     * Gibt eine Liste aller Kunden zurück.
     *
     * @return Liste von Customer-Objekten
     */
    @Operation(summary = "Alle Kunden abrufen", description = "Gibt eine Liste aller Kunden zurück.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich abgerufen",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Customer.class))))
    })
    @GetMapping("/customer")
    public List<Customer> getAllCustomers(){
        logger.info("call received");
        return customerService.getAllCustomers();
    }

    /**
     * Gibt einen einzelnen Kunden anhand der ID zurück.
     *
     * @param customerId ID des Kunden
     * @return Customer-Objekt oder 404, falls nicht gefunden
     */
    @Operation(summary = "Kunden nach ID abrufen", description = "Gibt einen einzelnen Kunden anhand der ID zurück.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kunde gefunden",
                    content = @Content(schema = @Schema(implementation = Customer.class))),
            @ApiResponse(responseCode = "404", description = "Kunde nicht gefunden")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long customerId){
        Customer customer = customerService.getCustomer(customerId);
        if(customer == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }
}
