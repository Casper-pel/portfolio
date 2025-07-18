package ip.project.backend.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ip.project.backend.backend.modeldto.CheckoutDto;
import ip.project.backend.backend.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
@Tag(name = "Checkout", description = "Checkout API for processing product purchases")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Autowired
    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Operation(summary = "Create Checkout Session", description = "Creates a new checkout session with the provided products")
    @ApiResponse(responseCode = "200", description = "Checkout session created successfully, returns session ID")
    @ApiResponse(responseCode = "400", description = "Error creating checkout session")
    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckoutSession(
            @Parameter(description = "List of products with quantities to checkout", required = true)
            @RequestBody CheckoutDto products) {
        Optional<String> sessionId = checkoutService.createCheckout(products);
        if (sessionId.isPresent()) {
            return ResponseEntity.ok(sessionId.get());
        } else {
            return ResponseEntity.status(400).body("Error creating checkout session");
        }
    }
}
