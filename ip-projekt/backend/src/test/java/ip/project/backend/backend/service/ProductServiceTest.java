package ip.project.backend.backend.service;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.StripeCollection;
import com.stripe.model.StripeSearchResult;
import com.stripe.param.PriceSearchParams;
import com.stripe.param.ProductListParams;
import com.stripe.param.ProductUpdateParams;
import com.stripe.service.PriceService;
import ip.project.backend.backend.modeldto.ProductDto;
import ip.project.backend.backend.modeldto.ProductPriceDto;
import ip.project.backend.backend.repository.OrderRepository;
import ip.project.backend.backend.util.StripeConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import com.stripe.service.ProductService;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private StripeConnection stripeConnection;

    @Mock
    private com.stripe.StripeClient stripeClient;

    @Mock
    private com.stripe.service.ProductService productServiceStripe;

    @Mock
    private com.stripe.service.PriceService priceServiceStripe;


    private ip.project.backend.backend.service.ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        OrderRepository orderRepository = mock(OrderRepository.class);
        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);
        when(stripeClient.products()).thenReturn(productServiceStripe);
        when(stripeClient.prices()).thenReturn(priceServiceStripe);

        productService = new ip.project.backend.backend.service.ProductService(stripeConnection, orderRepository);
    }




    @Test
    void addProduct_successfulCreation_thenReturnsProductId() throws StripeException {
        ProductDto productDto = mock(ProductDto.class);
        when(productDto.getProductName()).thenReturn("Testprodukt");
        when(productDto.getProductDescription()).thenReturn("Beschreibung");
        when(productDto.getUpcCode()).thenReturn("123456");
        when(productDto.getCurrency()).thenReturn("eur");
        when(productDto.getListPrice()).thenReturn(new BigDecimal("19.99"));
        when(productDto.getCostPrice()).thenReturn(new BigDecimal("9.99"));
        when(productDto.isTaxIncludedInPrice()).thenReturn(true);
        when(productDto.isActive()).thenReturn(true);

        Product mockedStripeProduct = mock(Product.class);
        when(mockedStripeProduct.getId()).thenReturn("prod_test_123");

        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);
        when(stripeClient.products()).thenReturn(productServiceStripe);
        when(productServiceStripe.create(any())).thenReturn(mockedStripeProduct);

        Optional<String> result = productService.addProduct(productDto);

        assertTrue(result.isPresent());
        assertEquals("prod_test_123", result.get());
    }

    @Test
    void testGetProductById_success() throws StripeException {
        String productId = "prod_001";

        Product mockStripeProduct = mock(Product.class);
        when(mockStripeProduct.getId()).thenReturn(productId);
        when(mockStripeProduct.getName()).thenReturn("Testprodukt");
        when(mockStripeProduct.getDescription()).thenReturn("Beschreibung");
        when(mockStripeProduct.getMetadata()).thenReturn(Map.of("upcCode", "123456"));
        when(mockStripeProduct.getActive()).thenReturn(true);

        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);
        when(stripeClient.products()).thenReturn(productServiceStripe);
        when(productServiceStripe.retrieve(productId)).thenReturn(mockStripeProduct);

        // Spy erstellen und Methode stubben
        ip.project.backend.backend.service.ProductService spyService = spy(productService);
        ProductDto dummyDto = mock(ProductDto.class);
        dummyDto.setProductId(productId);
        dummyDto.setProductName("Testprodukt");

        doReturn(Optional.of(dummyDto)).when(spyService).mapStripeProductToProductDto(mockStripeProduct);

        Optional<ProductDto> result = spyService.getProductById(productId);

        assertTrue(result.isPresent());
    }



    @Test
     void testGetPriceToProduct_Success() throws StripeException {
        String productId = "prod_123";
        String priceId = "price_123";

        Product product = new Product();
        product.setId(productId);
        product.setDefaultPrice(priceId);

        Price price = new Price();
        price.setId(priceId);
        price.setUnitAmount(1999L); // 19.99€

        when(productServiceStripe.retrieve(productId)).thenReturn(product);
        when(priceServiceStripe.retrieve(priceId)).thenReturn(price);

        Optional<ProductPriceDto> result = productService.getPriceToProduct(productId);

        assertTrue(result.isPresent());
        assertEquals(productId, result.get().getProductId());
        assertEquals(priceId, result.get().getPriceId());
        assertEquals(19.0f, result.get().getPrice()); // Achtung: Rundung beachten!
    }

    @Test
     void testGetPriceToProduct_ProductNotFound() throws StripeException {
        when(productServiceStripe.retrieve("invalid_id")).thenReturn(null);

        Optional<ProductPriceDto> result = productService.getPriceToProduct("invalid_id");

        assertFalse(result.isPresent());
    }

    @Test
     void testGetPriceToProduct_PriceNotFound() throws StripeException {
        Product product = new Product();
        product.setId("prod_123");
        product.setDefaultPrice("price_missing");

        when(productServiceStripe.retrieve("prod_123")).thenReturn(product);
        when(priceServiceStripe.retrieve("price_missing")).thenReturn(null);

        Optional<ProductPriceDto> result = productService.getPriceToProduct("prod_123");

        assertFalse(result.isPresent());
    }

    @Test
     void testGetPriceToProduct_StripeException() {

        Optional<ProductPriceDto> result = productService.getPriceToProduct("prod_123");

        assertFalse(result.isPresent());
    }



    @Test
    void getAllProducts_whenNoProducts_thenReturnEmptyList() throws StripeException {
        // Arrange
        StripeCollection<Product> emptyCollection = mock(StripeCollection.class);
        when(emptyCollection.getData()).thenReturn(new ArrayList<>());

        when(productServiceStripe.list()).thenReturn(emptyCollection);

        // Damit dein echter Code mit dem gleichen Param-Objekt aufruft:
        when(stripeClient.products()).thenReturn(productServiceStripe);
        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);

        // Act
        List<ProductDto> result = productService.getAllProducts();

        // Assert
        assertTrue(result.isEmpty());
    }
    @Test
    void getAllProducts_whenProductsExist_thenReturnList() throws StripeException {
        // Mock Product
        Product mockProduct = mock(Product.class);
        when(mockProduct.getId()).thenReturn("prod_123");
        when(mockProduct.getName()).thenReturn("Mock Product");
        when(mockProduct.getDescription()).thenReturn("Mock Description");
        when(mockProduct.getMetadata()).thenReturn(Map.of("upcCode", "1234567890"));
        when(mockProduct.getActive()).thenReturn(true);
        when(mockProduct.getCreated()).thenReturn(1620000000L);
        when(mockProduct.getUpdated()).thenReturn(1625000000L);
        when(mockProduct.getDefaultPrice()).thenReturn("price_123");

        // Mock Price
        Price mockPrice = mock(Price.class);
        when(mockPrice.getActive()).thenReturn(true);
        when(mockPrice.getCurrency()).thenReturn("eur");
        when(mockPrice.getTaxBehavior()).thenReturn("inclusive");
        when(mockPrice.getUnitAmountDecimal()).thenReturn(BigDecimal.valueOf(1000));
        when(mockPrice.getMetadata()).thenReturn(Map.of("costPrice", "700"));

        // Mock StripeCollection for Products
        StripeCollection<Product> stripeCollection = mock(StripeCollection.class);
        when(stripeCollection.getData()).thenReturn(List.of(mockProduct));

        // Mock Stripe client/service
        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);

        // ProductService
        when(stripeClient.products()).thenReturn(productServiceStripe);
        when(productServiceStripe.list(any(ProductListParams.class))).thenReturn(stripeCollection);

        // PriceService
        PriceService priceServiceMock = mock(PriceService.class);
        when(stripeClient.prices()).thenReturn(priceServiceMock);
        when(priceServiceMock.retrieve("price_123")).thenReturn(mockPrice);

        // Call the method
        List<ProductDto> result = productService.getAllProducts();

        // Assertions
        assertEquals(1, result.size());
        ProductDto dto = result.get(0);
        assertEquals("prod_123", dto.getProductId());
        assertEquals("Mock Product", dto.getProductName());
        assertEquals("Mock Description", dto.getProductDescription());
        assertEquals("eur", dto.getCurrency());
        assertEquals(true, dto.isTaxIncludedInPrice());
        assertEquals(BigDecimal.valueOf(700), dto.getCostPrice());
        assertEquals("1234567890", dto.getUpcCode());
        assertEquals(1620000000L, dto.getCreated());
        assertEquals(1625000000L, dto.getUpdated());
        assertEquals(true, dto.isActive());
        assertEquals("price_123", dto.getPriceId());
    }



    @Test
    void getActiveProducts_whenProductsExist_thenReturnMappedDtos() throws StripeException {

        // 1. Mocks für ein Stripe-Produkt
        Product mockProduct = mock(Product.class);
        when(mockProduct.getId()).thenReturn("prod_001");
        when(mockProduct.getName()).thenReturn("Testprodukt");
        when(mockProduct.getDescription()).thenReturn("Beschreibung");
        when(mockProduct.getMetadata()).thenReturn(Map.of("upcCode", "123456"));
        when(mockProduct.getCreated()).thenReturn(1620000000L);
        when(mockProduct.getUpdated()).thenReturn(1625000000L);
        when(mockProduct.getActive()).thenReturn(true);

        // 2. Mocks für einen Preis
        Price mockPrice = mock(Price.class);
        when(mockPrice.getUnitAmount()).thenReturn(999L);
        when(mockPrice.getCurrency()).thenReturn("eur");
        when(mockPrice.getTaxBehavior()).thenReturn("inclusive");
        when(mockPrice.getActive()).thenReturn(false);  // nur nötig wenn du .filter(price -> !price.getActive()) nutzt
        when(mockPrice.getMetadata()).thenReturn(Map.of("costPrice", "6.49"));

        // 3. Preis-Suchergebnis mocken
        StripeSearchResult<Price> priceSearchResult = mock(StripeSearchResult.class);
        when(priceSearchResult.getData()).thenReturn(List.of(mockPrice));

        // 4. StripeCollection für Produkte
        StripeCollection<Product> productCollection = mock(StripeCollection.class);
        when(productCollection.getData()).thenReturn(List.of(mockProduct));

        // 5. Stripe Mocks
        ProductService stripeProductService = mock(ProductService.class);
        PriceService stripePriceService = mock(PriceService.class);

        // 6. Mock-Verhalten konfigurieren
        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);
        when(stripeClient.products()).thenReturn(stripeProductService);
        when(stripeClient.prices()).thenReturn(stripePriceService);
        when(stripeProductService.list(any(ProductListParams.class))).thenReturn(productCollection);
        when(stripePriceService.search(any())).thenReturn(priceSearchResult);

        // 7. Methode aufrufen
        List<ProductDto> result = productService.getActiveProducts();

        // 8. Validieren
        assertEquals(1, result.size());
        ProductDto dto = result.get(0);
        assertEquals("Testprodukt", dto.getProductName());
        assertEquals("Beschreibung", dto.getProductDescription());
        assertEquals("123456", dto.getUpcCode());
        assertEquals("eur", dto.getCurrency());
        assertTrue(dto.isTaxIncludedInPrice());
    }



    @Test
    void testDeleteProduct_whenProductFound_thenDeactivate() throws Exception {
        // Arrange
        when(stripeClient.products()).thenReturn(productServiceStripe);

        String productId = "prod_123";

        Product mockProduct = mock(Product.class);
        when(productServiceStripe.retrieve(productId)).thenReturn(mockProduct);
        
        when(mockProduct.update(any(ProductUpdateParams.class))).thenReturn(mockProduct);        // Act
        Optional<String> result = productService.deleteProduct(productId);

        // Assert
        assertTrue(result.isEmpty());
    }

}
