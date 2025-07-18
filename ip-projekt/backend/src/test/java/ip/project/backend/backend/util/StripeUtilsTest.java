package ip.project.backend.backend.util;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.stripe.param.*;

import java.util.HashMap;
import java.util.Map;

class StripeUtilsTest {

    @Test
    void testCreateDefaultPriceData_basic() {
        ProductCreateParams.DefaultPriceData result = StripeUtils.createDefaultPriceData(
                "usd", 1000L, ProductCreateParams.DefaultPriceData.TaxBehavior.EXCLUSIVE, "50"
        );
        assertNotNull(result);
        assertEquals("usd", result.getCurrency());
        assertEquals(1000L, result.getUnitAmount());
        assertEquals("50", result.getMetadata().get("costPrice"));
        assertEquals(ProductCreateParams.DefaultPriceData.TaxBehavior.EXCLUSIVE, result.getTaxBehavior());
    }

    @Test
    void testCreateProductParams_basic() {
        ProductCreateParams.DefaultPriceData defaultPriceData = StripeUtils.createDefaultPriceData(
                "eur", 2000L, ProductCreateParams.DefaultPriceData.TaxBehavior.INCLUSIVE, "100"
        );
        Map<String, String> metadata = Map.of("key1", "value1");
        ProductCreateParams result = StripeUtils.createProductParams(
                "Test Product", "Description", true, metadata, defaultPriceData
        );
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals(true, result.getActive());
        assertEquals("value1", result.getMetadata().get("key1"));
        assertEquals(defaultPriceData, result.getDefaultPriceData());
    }

    @Test
    void testCreatePriceParams_basic() {
        Map<String, String> metadata = new HashMap<>();
        PriceCreateParams result = StripeUtils.createPriceParams(
                "usd", 500L, "prod_123", PriceCreateParams.TaxBehavior.EXCLUSIVE, metadata
        );
        assertNotNull(result);
        assertEquals("usd", result.getCurrency());
        assertEquals(500L, result.getUnitAmount());
        assertEquals("prod_123", result.getProduct());
        assertEquals(PriceCreateParams.TaxBehavior.EXCLUSIVE, result.getTaxBehavior());
    }

    @Test
    void testUpdateProductParams_basic() {
        Map<String, String> metadata = Map.of("metaKey", "metaValue");
        ProductUpdateParams result = StripeUtils.updateProductParams(
                "Name", "Desc", false, metadata, "price_456"
        );
        assertNotNull(result);
        assertEquals("Name", result.getName());
        assertEquals("Desc", result.getDescription());
        assertEquals(false, result.getActive());
        assertEquals("price_456", result.getDefaultPrice());
    }

    @Test
    void testCreatePriceSearchParams_basic() {
        PriceSearchParams result = StripeUtils.createPriceSearchParams("prod_abc");
        assertNotNull(result);
        assertTrue(result.getQuery().contains("product:'prod_abc'"));
    }


    @Test
    void testCreateMetadata_withNullProductDto() {
        Map<String, String> metadata = StripeUtils.createMetadata(null);
        assertNotNull(metadata);
        assertTrue(metadata.isEmpty());
    }
}
