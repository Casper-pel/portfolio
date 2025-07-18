package ip.project.backend.backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import ip.project.backend.backend.model.Checkout;
import ip.project.backend.backend.model.ProductWithId;
import ip.project.backend.backend.modeldto.CheckoutDto;
import ip.project.backend.backend.modeldto.ProductWithQuantity;
import ip.project.backend.backend.repository.CheckoutRepository;
import ip.project.backend.backend.util.StripeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CheckoutService {

    private static final String SUCCESS_URL = "http://localhost:3000/checkout/success";
    private final CheckoutRepository checkoutRepository;
    private final StripeConnection stripeConnection;
    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class);


    @Autowired
    public CheckoutService(CheckoutRepository checkoutRepository, StripeConnection stripeConnection) {
        this.checkoutRepository = checkoutRepository;
        this.stripeConnection = stripeConnection;

    }


    /**
     * creates a checkout session for stripe
     * @param checkoutDto the checkout object which should be saved
     * @return returns Optional List with Client Secret and Session id
     */
    public Optional<String> createCheckout(CheckoutDto checkoutDto) {
        if (checkoutDto.getProducts().isEmpty()) {
            logger.warn("No products provided for checkout.");
            return Optional.empty();
        }

        List<SessionCreateParams.LineItem> lineItem = createLineItems(checkoutDto);

        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomerEmail("nofill@localhost.local")
                .addAllLineItem(lineItem)
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setAllowPromotionCodes(true) // enable coupon codes
                .setReturnUrl(SUCCESS_URL)
                .build();

        try {
            // Nutzt jetzt deinen Stripe-Client über StripeConnection
            Session session = stripeConnection.getStripeClient().checkout().sessions().create(params);
            logger.info("Created checkout session: {}", session.getId());
            insertCheckout(checkoutDto, session.getId());

            return Optional.of(session.getClientSecret());
        } catch (StripeException e) {
            logger.error("Error creating Stripe checkout session: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * creates the line items for the stripe checkout session
     * @param checkoutDto checkout object
     */
    private List<SessionCreateParams.LineItem> createLineItems(CheckoutDto checkoutDto) {
        logger.debug("Creating line items for checkout with {} products", checkoutDto.getProducts().size());
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (ProductWithQuantity product : checkoutDto.getProducts()) {
            logger.debug("Adding product with ID: {}, price: {}, quantity: {}", 
                    product.getProductId(), product.getPrice(), product.getQuantity());
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPrice(product.getPrice()) // assumes this is the Stripe Price ID
                    .setQuantity((long)product.getQuantity()) // assume ProductWithQuantity has getQuantity()
                    .build();
            lineItems.add(lineItem);
        }

        logger.debug("Created {} line items for checkout", lineItems.size());
        return lineItems;
    }


    /**
     * inserts checkout into db
     * @param checkoutDto objekt which holds all products which have been sold
     * @param sessionId current sessionId
     */
    public void insertCheckout(CheckoutDto checkoutDto, String sessionId) {
        logger.info("Inserting checkout with session ID: {} and {} products", 
                sessionId, checkoutDto.getProducts().size());

        List<ProductWithId> products = checkoutDto.getProducts().stream()
                .map(dto -> {
                    logger.debug("Converting product with ID: {} and quantity: {}", 
                            dto.getProductId(), dto.getQuantity());
                    return new ProductWithId(
                            dto.getProductId(),
                            dto.getQuantity()
                    );
                })
                .toList();

        Checkout checkout = new Checkout(sessionId, new Date(), products);
        logger.debug("Created checkout object with session ID: {} and {} products", 
                sessionId, products.size());

        try {
            checkoutRepository.insert(checkout);
            logger.info("Successfully inserted checkout with session ID: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error inserting checkout with session ID {}: {}", 
                    sessionId, e.getMessage(), e);
            throw e;
        }
    }

}
