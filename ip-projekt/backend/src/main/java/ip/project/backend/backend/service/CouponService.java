package ip.project.backend.backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.StripeCollection;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CouponListParams;
import com.stripe.param.PromotionCodeCreateParams;
import ip.project.backend.backend.modeldto.CouponDto;
import ip.project.backend.backend.modeldto.NewCouponDto;
import ip.project.backend.backend.util.StripeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ip.project.backend.backend.util.StripeUtils.createCouponParams;

@Service
public class CouponService {

    private final Logger logger = LoggerFactory.getLogger(CouponService.class);
    private final StripeConnection stripeConnection;

    // to fasten up checks with stripe we keep a local copy of the coupons to avoid unnecessary calls
    // this is updated if a coupon is created, updated or deleted
    private List<CouponDto> couponDtoList = null;


    @Autowired
    public CouponService(StripeConnection stripeConnection) {
        this.stripeConnection = stripeConnection;
    }


    /**
     * Creates a new coupon in the stripe system
     * checks if coupon with same name already exists
     *
     * @return returns true if valid and successfully created, false otherwise
     */
    public boolean createCoupon(NewCouponDto newCouponDto) {

        // if not initialized, initialize the local cache
        if (couponDtoList == null) {
            getCouponsFromStripe();
        }

        if (!validateNewCoupon(newCouponDto)) {
            logger.error("New coupon validation failed");
            return false;
        }

        if (!newCouponDto.getCurrency().equals("eur") && !newCouponDto.getCurrency().equals("usd")) {
            logger.error("Invalid currency provided. Only 'eur' and 'usd' are supported.");
            return false;
        }

        CouponCreateParams.Duration duration;

        // map the duration string to the enum
        switch (newCouponDto.getDuration()) {
            case "once":
                duration = CouponCreateParams.Duration.ONCE;
                break;
            case "forever":
                duration = CouponCreateParams.Duration.FOREVER;
                break;
            default:
                logger.error("Invalid duration parameter provided");
                return false;
        }

        CouponCreateParams params = createCouponParams(newCouponDto, duration);

        try {
            // Create the coupon
            Coupon coupon = stripeConnection.getStripeClient().coupons().create(params);

            // Create the promotion code with the same name as the coupon
            PromotionCodeCreateParams promotionCodeParams = PromotionCodeCreateParams.builder()
                    .setCoupon(coupon.getId())
                    .setCode(newCouponDto.getName())
                    .build();

            stripeConnection.getStripeClient().promotionCodes().create(promotionCodeParams);

            addNewCouponToList(coupon);
        } catch (StripeException e) {
            logger.error("Error while creating coupon or promotion code", e);
            return false;
        }

        return true;
    }


    /**
     * Returns a list of all coupons
     * This method retrieves all coupons from the local cache if available, otherwise it fetches them from Stripe.
     *
     * @return a list of CouponDto objects representing all coupons
     */
    public List<CouponDto> getAllCoupons() {

        // check if local cache is available and return
        if (couponDtoList != null) {
            return couponDtoList;
        }

        getCouponsFromStripe();

        return couponDtoList;
    }

    /**
     * Deletes a coupon by its name.
     *
     * @param name the name of the coupon to delete
     * @return true if the coupon was successfully deleted, false otherwise
     */
    public boolean deleteCoupon(String name) {
        Optional<CouponDto> couponDto = getCouponByName(name);
        if (couponDto.isEmpty()) {
            logger.error("Coupon name not found");
            return false;
        }

        try {
            // remove from stripe
            stripeConnection.getStripeClient().coupons().delete(couponDto.get().getId());

            // remove from local cache
            couponDtoList.remove(couponDto.get());
            return true;
        } catch (StripeException e) {
            logger.error("Error while deleting coupon", e);
            return false;
        }
    }


    /**
     * Maps the Stripe CouponCollection to a list of CouponDto objects.
     * writes the data to the local cache
     *
     * @param collection the CouponCollection object from Stripe
     */
    void mapCollectionToDtoObject(StripeCollection<Coupon> collection) {
        this.couponDtoList = collection.getData()
                .parallelStream()
                .map(coupon -> {
                    CouponDto dto = new CouponDto();
                    dto.setId(coupon.getId());
                    dto.setName(coupon.getName());
                    dto.setAmountOff(coupon.getAmountOff() != null ? coupon.getAmountOff().intValue() : null);
                    dto.setCurrency(coupon.getCurrency() == null ? "eur" : coupon.getCurrency());
                    dto.setDuration(coupon.getDuration());
                    dto.setPercentOff(coupon.getPercentOff() != null ? coupon.getPercentOff().floatValue() : null);
                    return dto;
                })
                .collect(Collectors.toCollection(ArrayList::new)); // ✅ mutable!

    }


    /**
     * for an order the coupon with the given name is searched
     *
     * @param name the name of the coupon to search for
     * @return the CouponDto object if found, null otherwise
     */
    public Optional<CouponDto> getCouponByName(String name) {
        if (couponDtoList == null) {
            couponDtoList = new ArrayList<>();
        }
        return couponDtoList
                .parallelStream()
                .filter(coupon -> coupon.getName().equals(name))
                .findFirst();
    }


    /**
     * if a new coupon is created, this method validates the new coupon
     * it checks wether a coupon with the same name, duration, currency and discount already exists
     * and whether either amountOff or percentOff is set, but not both
     *
     * @param newCouponDto the new coupon to validate
     * @return true if the new coupon is valid, false otherwise
     */
    boolean validateNewCoupon(NewCouponDto newCouponDto) {
        // check if name already exists
        boolean nameExists = couponDtoList.stream()
                .anyMatch(existing -> existing.getName().equalsIgnoreCase(newCouponDto.getName()));

        if (nameExists) {
            logger.error("A coupon with that name already exists");
            return false;
        }

        // New logic: check for exclusive discount
        boolean hasAmountOff = newCouponDto.getAmountOff() != 0;
        boolean hasPercentOff = newCouponDto.getPercentOff() != 0.0f;

        if (hasAmountOff == hasPercentOff) {
            logger.error("Exactly one discount (amountOff or percentOff) must be set");
            return false;
        }

        // check for duplicate duration + same discount
        boolean duplicateDiscountWithDuration = couponDtoList.stream()
                .anyMatch(existing ->
                        Objects.equals(existing.getDuration(), newCouponDto.getDuration()) &&
                                (
                                        (hasAmountOff && Objects.equals(existing.getAmountOff(), newCouponDto.getAmountOff())) ||
                                                (hasPercentOff && Objects.equals(existing.getPercentOff(), newCouponDto.getPercentOff()))
                                )
                );

        if (duplicateDiscountWithDuration) {
            logger.error("A coupon with the same duration and discount already exists");
            return false;
        }

        return true;
    }



    /**
     * Creates and Adds a new coupon to the local cache.
     *
     * @param coupon new coupon from stripe
     */
    void addNewCouponToList(Coupon coupon) {
        CouponDto dto = new CouponDto();
        dto.setId(coupon.getId());
        dto.setName(coupon.getName());
        dto.setAmountOff(coupon.getAmountOff() != null ? coupon.getAmountOff().intValue() : null);
        dto.setCurrency(coupon.getCurrency());
        dto.setDuration(coupon.getDuration());
        dto.setPercentOff(coupon.getPercentOff() != null ? coupon.getPercentOff().floatValue() : null);

        couponDtoList.add(dto);
    }


    /**
     * Fetches all coupons from Stripe and updates the local cache.
     * used when the application starts and the cache is not initialized. therefore it is not called frequently
     */
    void getCouponsFromStripe() {
        CouponListParams params = CouponListParams.builder().setLimit(100L).build();
        try {
            StripeCollection<Coupon> coupons = stripeConnection.getStripeClient().coupons().list(params);
            mapCollectionToDtoObject(coupons);

        } catch (StripeException e) {
            logger.error(e.getMessage());
            couponDtoList = new ArrayList<>();
        }
    }

}