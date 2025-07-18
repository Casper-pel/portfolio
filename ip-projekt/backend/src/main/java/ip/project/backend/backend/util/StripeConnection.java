package ip.project.backend.backend.util;

import com.stripe.StripeClient;
import org.springframework.stereotype.Component;

@Component
public class StripeConnection {

    private final StripeClient stripeClient;

    public StripeConnection() {
        String stripeKey = System.getenv("STRIPE_SECRETKEY");

        if(stripeKey == null || stripeKey.isEmpty()) {
            throw new IllegalArgumentException("Stripe secret key is not set in environment variables.");
        }
        stripeClient = new StripeClient(stripeKey);
    }

    public StripeClient getStripeClient() {
        return stripeClient;
    }

}
