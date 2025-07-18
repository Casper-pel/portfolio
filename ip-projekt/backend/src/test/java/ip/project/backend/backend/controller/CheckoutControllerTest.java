package ip.project.backend.backend.controller;

import ip.project.backend.backend.modeldto.CheckoutDto;
import ip.project.backend.backend.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutController checkoutController;

    @Mock
    private CheckoutDto checkoutDto;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCheckoutSession_returnsOk_whenSessionIdPresent() {
        String expectedSessionId = "sess_123";

        when(checkoutService.createCheckout(checkoutDto)).thenReturn(Optional.of(expectedSessionId));

        ResponseEntity<String> response = checkoutController.createCheckoutSession(checkoutDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedSessionId, response.getBody());
    }

    @Test
    void createCheckoutSession_returnsBadRequest_whenSessionIdEmpty() {
        when(checkoutService.createCheckout(checkoutDto)).thenReturn(Optional.empty());

        ResponseEntity<String> response = checkoutController.createCheckoutSession(checkoutDto);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Error creating checkout session", response.getBody());
    }
}
