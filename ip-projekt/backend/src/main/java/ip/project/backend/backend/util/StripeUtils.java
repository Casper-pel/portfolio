package ip.project.backend.backend.util;

import com.stripe.param.*;
import ip.project.backend.backend.modeldto.NewCouponDto;
import ip.project.backend.backend.modeldto.ProductDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * this class contains all methos used in service classes to create stripe objects
 * for updating, creating, deleting and getting stripe objects
 */
@Component
public class StripeUtils {


    private StripeUtils() {
    }


    public static ProductCreateParams.DefaultPriceData createDefaultPriceData(String currency, Long unitAmount, ProductCreateParams.DefaultPriceData.TaxBehavior taxBehavior, String costPrice) {
        return ProductCreateParams.DefaultPriceData.builder()
                .setCurrency(currency)
                .setUnitAmount(unitAmount)
                .setTaxBehavior(taxBehavior)
                .putMetadata("costPrice", costPrice)
                .build();
    }

    public static ProductCreateParams createProductParams(String name, String description, boolean active, Map<String, String> metadata, ProductCreateParams.DefaultPriceData defaultPriceData) {
        return ProductCreateParams.builder()
                .setName(name)
                .setDescription(description)
                .putAllMetadata(metadata)
                .setActive(active)
                .setDefaultPriceData(defaultPriceData)
                .build();
    }

    public static PriceCreateParams createPriceParams(String currency, Long unitAmount, String productId, PriceCreateParams.TaxBehavior taxBehavior, Map<String, String> metadata) {
        return PriceCreateParams.builder()
                .setCurrency(currency)
                .setUnitAmount(unitAmount)
                .setProduct(productId)
                .setTaxBehavior(taxBehavior)
                .putAllMetadata(metadata)
                .build();
    }

    public static ProductUpdateParams updateProductParams(String productName, String productDescription, boolean active, Map<String, String> metadata, String newPriceId) {
        return ProductUpdateParams.builder()
                .setName(productName)
                .setDescription(productDescription)
                .putAllMetadata(metadata)
                .setActive(active)
                .setDefaultPrice(newPriceId)
                .build();
    }

    public static CouponCreateParams createCouponParams(NewCouponDto dto, CouponCreateParams.Duration duration) {
        CouponCreateParams.Builder builder = CouponCreateParams.builder()
                .setName(dto.getName())
                .setDuration(duration);

        boolean hasAmountOff = dto.getAmountOff() != null && dto.getAmountOff() > 0;
        boolean hasPercentOff = dto.getPercentOff() != null && dto.getPercentOff() > 0;

        if (hasAmountOff && hasPercentOff) {
            // Logik: Wenn beides gesetzt ist, priorisiere amountOff (kannst du bei Bedarf umdrehen)
            dto.setPercentOff(null);
        } else if (hasPercentOff) {
            dto.setAmountOff(null);
        }

        if (dto.getAmountOff() != null) {
            builder.setAmountOff(Long.valueOf(dto.getAmountOff()))
                    .setCurrency(dto.getCurrency());
        } else if (dto.getPercentOff() != null) {
            builder.setPercentOff(BigDecimal.valueOf(dto.getPercentOff()));
        } else {
            throw new IllegalArgumentException("Entweder amountOff oder percentOff muss gesetzt sein.");
        }

        return builder.build();
    }


    public static PriceSearchParams createPriceSearchParams(String productId) {
        return PriceSearchParams.builder()
                .setQuery("active:'false' AND product:'" + productId + "'")
                .build();
    }


    public static Map<String, String> createMetadata(ProductDto productDto) {
        Map<String, String> metadata = new HashMap<>();

        if (productDto == null) {
            return metadata;
        }
        metadata.put("upcCode", productDto.getUpcCode());
        return metadata;
    }


}
