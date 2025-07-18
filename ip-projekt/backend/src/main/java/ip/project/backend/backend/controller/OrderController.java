package ip.project.backend.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ip.project.backend.backend.model.Order;
import ip.project.backend.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/order")
@Tag(name = "Order", description = "API für Bestellungen")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(
            summary = "Hole Bestellungen zwischen zwei Zeitpunkten",
            description = "Gibt alle Bestellungen im angegebenen Zeitraum zurück. Optional kann nach Mitarbeiter-ID gefiltert werden."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Erfolgreich gefunden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Ungültige Anfrageparameter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Interner Serverfehler", content = @Content)
    })
    @GetMapping("/between")
    public ResponseEntity<List<Order>> getOrdersBetween(
            @Parameter(description = "Startzeitpunkt im ISO 8601 Format", required = true)
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
            @Parameter(description = "Endzeitpunkt im ISO 8601 Format", required = true)
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end,
            @Parameter(description = "Optionale Mitarbeiter-ID zur Filterung")
            @RequestParam(value = "employeeId", required = false) Integer employeeId) {

        logger.info("Request received to get orders between {} and {}{}", 
                start, end, employeeId != null ? " for employee ID: " + employeeId : "");

        try {
            List<Order> orders;
            if (employeeId != null) {
                logger.debug("Filtering orders by employee ID: {}", employeeId);
                orders = orderService.getOrdersInPeriodByEmployee(start, end, employeeId);
            } else {
                logger.debug("Retrieving all orders in the specified period");
                orders = orderService.getOrdersInPeriod(start, end);
            }

            logger.info("Found {} orders in the specified period{}", 
                    orders.size(), employeeId != null ? " for employee ID: " + employeeId : "");
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving orders: {}", e.getMessage(), e);
            throw e;
        }
    }
}
