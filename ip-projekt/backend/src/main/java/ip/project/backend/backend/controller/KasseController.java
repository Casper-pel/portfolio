package ip.project.backend.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import ip.project.backend.backend.modeldto.OrderDto;
import ip.project.backend.backend.service.OrderService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("api/kassa")
@Tag(name = "Kassa", description = "API für Kassiervorgänge")
public class KasseController {

    private static final Logger logger = LoggerFactory.getLogger(KasseController.class);

    private static final String SUCCESS = "success";

    private final OrderService orderService;

    @Autowired
    public KasseController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(
            summary = "Kassiervorgang abschließen",
            description = "Erstellt eine neue Bestellung mit zufällig generierter Bestell-ID, Produktnamen, Gesamtpreis, Datum und Mitarbeiter-ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bestellung erfolgreich erstellt",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe oder Bestellung existiert bereits",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Serverfehler beim Verarbeiten der Bestellung",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(
            @Parameter(description = "DTO mit Bestelldaten", required = true)
            @NotNull @Valid @RequestBody OrderDto orderDto) {

        logger.info("Checkout request received with {} products", 
                orderDto.getProductNames() != null ? orderDto.getProductNames().size() : 0);

        try {
            String orderId = UUID.randomUUID().toString();
            List<String> productNames = orderDto.getProductNames();
            BigDecimal totalPrice = orderDto.getTotalPrice();
            Date date = orderDto.getDate();
            Integer employeeId = orderDto.getEmployeeId();

            logger.debug("Processing checkout: orderId={}, products={}, totalPrice={}, employeeId={}", 
                    orderId, productNames.size(), totalPrice, employeeId);

            if (orderService.orderExists(orderId)) {
                logger.warn("Checkout failed: Order with ID {} already exists", orderId);
                return ResponseEntity.status(400).body(Map.of(
                        SUCCESS, false,
                        "error", "An order with this ID already exists"
                ));
            }

            orderService.createOrder(orderId, productNames, totalPrice, date, employeeId);
            logger.info("Order created successfully: orderId={}, totalPrice={}, employeeId={}", 
                    orderId, totalPrice, employeeId);

            return ResponseEntity.ok(Map.of(SUCCESS, true));

        } catch (Exception e) {
            logger.error("Error processing checkout: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    SUCCESS, false,
                    "error", "Server error occurred: " + e.getMessage()
            ));
        }
    }
}
