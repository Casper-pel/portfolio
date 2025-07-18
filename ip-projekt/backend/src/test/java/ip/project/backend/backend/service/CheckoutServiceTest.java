package ip.project.backend.backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import ip.project.backend.backend.model.Checkout;
import ip.project.backend.backend.modeldto.CheckoutDto;
import ip.project.backend.backend.modeldto.ProductWithQuantity;
import ip.project.backend.backend.repository.CheckoutRepository;
import ip.project.backend.backend.util.StripeConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutServiceTest {

    private CheckoutRepository checkoutRepository;
    private StripeConnection stripeConnection;
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        checkoutRepository = mock(CheckoutRepository.class);
        stripeConnection = mock(StripeConnection.class);
        checkoutService = new CheckoutService(checkoutRepository, stripeConnection);
    }

    @Test
    void testCreateCheckout_withEmptyProducts_returnsEmptyOptional() {
        // Vollständig gemocktes CheckoutDto
        CheckoutDto checkoutDto = mock(CheckoutDto.class);
        when(checkoutDto.getProducts()).thenReturn(List.of()); // leer

        Optional<String> result = checkoutService.createCheckout(checkoutDto);

        assertTrue(result.isEmpty());
        verify(checkoutDto).getProducts();
        verifyNoInteractions(stripeConnection);
        verifyNoInteractions(checkoutRepository);
    }

    @Test
    void testCreateCheckout_withValidProducts_returnsClientSecret() throws StripeException {
        // Produkt mocken
        ProductWithQuantity product = mock(ProductWithQuantity.class);
        when(product.getPrice()).thenReturn("price_123");
        when(product.getQuantity()).thenReturn(2);
        when(product.getProductId()).thenReturn("p1");

        // CheckoutDto mocken
        CheckoutDto checkoutDto = mock(CheckoutDto.class);
        when(checkoutDto.getProducts()).thenReturn(List.of(product));

        // Stripe Session mocken
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("sess_123");
        when(session.getClientSecret()).thenReturn("secret_abc");

        // Stripe-Client verhalten mocken
        var stripeClient = mock(com.stripe.StripeClient.class, RETURNS_DEEP_STUBS);
        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);
        when(stripeClient.checkout().sessions().create(any(SessionCreateParams.class))).thenReturn(session);

        // Service aufrufen
        Optional<String> result = checkoutService.createCheckout(checkoutDto);

        // Assertions und Verifications
        assertTrue(result.isPresent());
        assertEquals("secret_abc", result.get());
        verify(checkoutRepository).insert(any(Checkout.class));
    }

    @Test
    void testInsertCheckout_savesToDatabase_withMocks() {
        // Produkt vollständig mocken
        ProductWithQuantity product = mock(ProductWithQuantity.class);
        when(product.getProductId()).thenReturn("prod123");
        when(product.getQuantity()).thenReturn(1);

        // CheckoutDto mocken
        CheckoutDto checkoutDto = mock(CheckoutDto.class);
        when(checkoutDto.getProducts()).thenReturn(List.of(product));

        // Methode aufrufen
        checkoutService.insertCheckout(checkoutDto, "sess123");

        // ArgumentCaptor verwenden
        ArgumentCaptor<Checkout> captor = ArgumentCaptor.forClass(Checkout.class);
        verify(checkoutRepository).insert(captor.capture());

        // Überprüfen der gespeicherten Daten
        Checkout savedCheckout = captor.getValue();
        assertEquals("sess123", savedCheckout.getSessionId());
        assertNotNull(savedCheckout.getDate());
        assertFalse(savedCheckout.getProducts().isEmpty());
        assertEquals("prod123", savedCheckout.getProducts().get(0).getProductId());
        assertEquals(1, savedCheckout.getProducts().get(0).getQuantity());
    }

}
