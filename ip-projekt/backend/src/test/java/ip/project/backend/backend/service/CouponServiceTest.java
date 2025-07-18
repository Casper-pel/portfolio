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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponServiceTest {

    @Mock
    private StripeConnection stripeConnection;

    @Mock
    private com.stripe.StripeClient stripeClient;

    @Mock
    private com.stripe.service.CouponService couponServiceStripe;

    @Mock
    private com.stripe.service.PromotionCodeService promotionCodeService;

    private ip.project.backend.backend.service.CouponService couponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);
        when(stripeClient.coupons()).thenReturn(couponServiceStripe);
        when(stripeClient.promotionCodes()).thenReturn(promotionCodeService);

        couponService = new ip.project.backend.backend.service.CouponService(stripeConnection);
    }

    @Test
    void createCoupon_Success() throws StripeException {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("TestCoupon", 1000, "eur", "once", 0.0f);

        // Mock empty coupon list initially
        StripeCollection<Coupon> emptyCollection = mock(StripeCollection.class);
        when(emptyCollection.getData()).thenReturn(new ArrayList<>());
        when(couponServiceStripe.list(any(CouponListParams.class))).thenReturn(emptyCollection);

        // Mock successful coupon creation
        Coupon mockCoupon = mock(Coupon.class);
        when(mockCoupon.getId()).thenReturn("coupon_123");
        when(mockCoupon.getName()).thenReturn("TestCoupon");
        when(mockCoupon.getAmountOff()).thenReturn(1000L);
        when(mockCoupon.getCurrency()).thenReturn("eur");
        when(mockCoupon.getDuration()).thenReturn("once");
        when(mockCoupon.getPercentOff()).thenReturn(null);

        when(couponServiceStripe.create(any(CouponCreateParams.class))).thenReturn(mockCoupon);

        // Act
        boolean result = couponService.createCoupon(newCouponDto);

        // Assert
        assertTrue(result);
        verify(couponServiceStripe, times(1)).create(any(CouponCreateParams.class));
        verify(promotionCodeService, times(1)).create(any(PromotionCodeCreateParams.class));
    }

    @Test
    void createCoupon_InvalidCurrency() throws StripeException {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("TestCoupon", 1000, "gbp", "once", 0.0f);

        // Mock empty coupon list initially
        StripeCollection<Coupon> emptyCollection = mock(StripeCollection.class);
        when(emptyCollection.getData()).thenReturn(new ArrayList<>());
        when(couponServiceStripe.list(any(CouponListParams.class))).thenReturn(emptyCollection);

        // Act
        boolean result = couponService.createCoupon(newCouponDto);

        // Assert
        assertFalse(result);
        verify(couponServiceStripe, never()).create(any(CouponCreateParams.class));
    }

    @Test
    void createCoupon_InvalidDuration() throws StripeException {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("TestCoupon", 1000, "eur", "repeating", 0.0f);

        // Mock empty coupon list initially
        StripeCollection<Coupon> emptyCollection = mock(StripeCollection.class);
        when(emptyCollection.getData()).thenReturn(new ArrayList<>());
        when(couponServiceStripe.list(any(CouponListParams.class))).thenReturn(emptyCollection);

        // Act
        boolean result = couponService.createCoupon(newCouponDto);

        // Assert
        assertFalse(result);
        verify(couponServiceStripe, never()).create(any(CouponCreateParams.class));
    }


    @Test
    void createCoupon_StripeException() throws StripeException {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("TestCoupon", 1000, "eur", "once", 0.0f);

        // Mock empty coupon list initially
        StripeCollection<Coupon> emptyCollection = mock(StripeCollection.class);
        when(emptyCollection.getData()).thenReturn(new ArrayList<>());
        when(couponServiceStripe.list(any(CouponListParams.class))).thenReturn(emptyCollection);

        // Mock StripeException
        when(couponServiceStripe.create(any(CouponCreateParams.class))).thenThrow(new StripeException("Stripe error", "request_id", "code", 400) {});

        // Act
        boolean result = couponService.createCoupon(newCouponDto);

        // Assert
        assertFalse(result);
        verify(couponServiceStripe, times(1)).create(any(CouponCreateParams.class));
    }

    @Test
    void getAllCoupons_Success() throws StripeException {
        // Arrange
        List<Coupon> couponList = new ArrayList<>();
        Coupon mockCoupon = mock(Coupon.class);
        when(mockCoupon.getId()).thenReturn("coupon_123");
        when(mockCoupon.getName()).thenReturn("TestCoupon");
        couponList.add(mockCoupon);

        StripeCollection<Coupon> stripeCollection = mock(StripeCollection.class);
        when(stripeCollection.getData()).thenReturn(couponList);

        when(couponServiceStripe.list(any(CouponListParams.class))).thenReturn(stripeCollection);

        // Act
        List<CouponDto> result = couponService.getAllCoupons();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("coupon_123", result.get(0).getId());
        assertEquals("TestCoupon", result.get(0).getName());
    }

    @Test
    void getAllCoupons_EmptyList() throws StripeException {
        // Arrange
        StripeCollection<Coupon> stripeCollection = mock(StripeCollection.class);
        when(stripeCollection.getData()).thenReturn(new ArrayList<>());

        when(couponServiceStripe.list(any(CouponListParams.class))).thenReturn(stripeCollection);

        // Act
        List<CouponDto> result = couponService.getAllCoupons();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllCoupons_StripeException() throws StripeException {
        // Arrange
        StripeException stripeException = mock(StripeException.class);
        when(couponServiceStripe.list(any(CouponListParams.class))).thenThrow(stripeException);

        // Act
        List<CouponDto> result = couponService.getAllCoupons();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteCoupon_Success() throws StripeException {
        // Arrange
        String couponName = "TestCoupon";

        // Setup mock coupon in the service's cache
        CouponDto mockCouponDto = new CouponDto();
        mockCouponDto.setId("coupon_123");
        mockCouponDto.setName(couponName);

        List<CouponDto> couponList = new ArrayList<>();
        couponList.add(mockCouponDto);

        // Use reflection to set the couponDtoList field
        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, couponList);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        boolean result = couponService.deleteCoupon(couponName);

        // Assert
        assertTrue(result);
        verify(couponServiceStripe, times(1)).delete(mockCouponDto.getId());
    }

    @Test
    void deleteCoupon_CouponNotFound() throws StripeException {
        // Arrange
        String couponName = "NonExistentCoupon";

        // Setup empty coupon list
        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        boolean result = couponService.deleteCoupon(couponName);

        // Assert
        assertFalse(result);
        verify(couponServiceStripe, never()).delete(anyString());
    }

    @Test
    void deleteCoupon_StripeException() throws StripeException {
        // Arrange
        String couponName = "TestCoupon";

        // Setup mock coupon in the service's cache
        CouponDto mockCouponDto = new CouponDto();
        mockCouponDto.setId("coupon_123");
        mockCouponDto.setName(couponName);

        List<CouponDto> couponList = new ArrayList<>();
        couponList.add(mockCouponDto);

        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, couponList);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        StripeException stripeException = mock(StripeException.class);
        when(couponServiceStripe.delete(anyString())).thenThrow(stripeException);

        // Act
        boolean result = couponService.deleteCoupon(couponName);

        // Assert
        assertFalse(result);
        verify(couponServiceStripe, times(1)).delete(mockCouponDto.getId());
    }

    @Test
    void getCouponByName_Found() {
        // Arrange
        String couponName = "TestCoupon";

        // Setup mock coupon in the service's cache
        CouponDto mockCouponDto = new CouponDto();
        mockCouponDto.setId("coupon_123");
        mockCouponDto.setName(couponName);

        List<CouponDto> couponList = new ArrayList<>();
        couponList.add(mockCouponDto);

        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, couponList);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        Optional<CouponDto> result = couponService.getCouponByName(couponName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(couponName, result.get().getName());
        assertEquals("coupon_123", result.get().getId());
    }

    @Test
    void getCouponByName_NotFound() {
        // Arrange
        String couponName = "NonExistentCoupon";

        // Setup empty coupon list
        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        Optional<CouponDto> result = couponService.getCouponByName(couponName);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void validateNewCoupon_ValidAmountOff() {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("NewCoupon", 1000, "eur", "once", 0.0f);

        // Setup empty coupon list
        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        boolean result = couponService.validateNewCoupon(newCouponDto);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateNewCoupon_ValidPercentOff() {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("NewCoupon", 0, "eur", "once", 25.0f);

        // Setup empty coupon list
        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        boolean result = couponService.validateNewCoupon(newCouponDto);

        // Assert
        assertTrue(result);
    }

    @Test
    void validateNewCoupon_BothDiscountsSet() {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("NewCoupon", 1000, "eur", "once", 25.0f);

        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        boolean result = couponService.validateNewCoupon(newCouponDto);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateNewCoupon_NoDiscountSet() {
        // Arrange
        NewCouponDto newCouponDto = new NewCouponDto("NewCoupon", 0, "eur", "once", 0.0f);

        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, new ArrayList<>());
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        boolean result = couponService.validateNewCoupon(newCouponDto);

        // Assert
        assertFalse(result);
    }

    @Test
    void validateNewCoupon_NameAlreadyExists() {
        // Arrange
        String existingName = "ExistingCoupon";
        NewCouponDto newCouponDto = new NewCouponDto(existingName, 1000, "eur", "once", 0.0f);

        CouponDto existingCouponDto = new CouponDto();
        existingCouponDto.setId("coupon_123");
        existingCouponDto.setName(existingName);
        existingCouponDto.setCurrency("usd");
        existingCouponDto.setDuration("forever");
        existingCouponDto.setAmountOff(500);

        List<CouponDto> couponList = new ArrayList<>();
        couponList.add(existingCouponDto);

        try {
            java.lang.reflect.Field field = couponService.getClass().getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, couponList);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        boolean result = couponService.validateNewCoupon(newCouponDto);

        // Assert
        assertFalse(result);
    }


    @Test
    void validateNewCoupon_DuplicateDiscountWithDuration() {
        // Arrange
        CouponService couponService = new CouponService(mock(StripeConnection.class));

        // Bestehenden Coupon simulieren
        CouponDto existingCouponDto = new CouponDto();
        existingCouponDto.setId("coupon_123");
        existingCouponDto.setName("ExistingCoupon");
        existingCouponDto.setCurrency("usd");
        existingCouponDto.setDuration("once");
        existingCouponDto.setAmountOff(1000);  // matcht genau mit neuem

        // interne Liste vorbereiten
        List<CouponDto> existingCoupons = new ArrayList<>();
        existingCoupons.add(existingCouponDto);

        // mit Reflection lokale Liste setzen (Alternative unten ohne Reflection!)
        try {
            Field field = CouponService.class.getDeclaredField("couponDtoList");
            field.setAccessible(true);
            field.set(couponService, existingCoupons);
        } catch (Exception e) {
            fail("Fehler beim Setzen der couponDtoList: " + e.getMessage());
        }

        // Neuer Coupon mit gleicher duration + amountOff
        NewCouponDto newCouponDto = new NewCouponDto("NewCoupon", 1000, "usd", "once", 0.0f);

        // Act
        boolean result = couponService.validateNewCoupon(newCouponDto);

        // Assert
        assertFalse(result, "Es sollte false zurückgegeben werden, da Duplikat existiert.");
    }


}
