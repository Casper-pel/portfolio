package ip.project.backend.backend.service;

import com.stripe.StripeClient;
import com.stripe.model.Product;
import com.stripe.service.ProductService;
import ip.project.backend.backend.mapper.StockMapper;
import ip.project.backend.backend.model.Stock;
import ip.project.backend.backend.modeldto.StockDto;
import ip.project.backend.backend.repository.StockRepository;
import ip.project.backend.backend.util.StripeConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StripeConnection stripeConnection;

    @Mock
    private com.stripe.StripeClient stripeClient;

    @Mock
    private com.stripe.model.Product stripeProduct;

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockMapper stockMapper;

    @Mock
    private ProductService productService;

    @BeforeEach
    void setup() {
        stripeClient = mock(StripeClient.class);
        productService = mock(ProductService.class);
        stockRepository = mock(StockRepository.class);
        stripeConnection = mock(StripeConnection.class);
        stripeProduct = mock(Product.class);
        stockMapper = mock(StockMapper.class);

        when(stripeConnection.getStripeClient()).thenReturn(stripeClient);

        lenient().when(stripeClient.products()).thenReturn(productService);

        stockService = new StockService(stockRepository, stripeConnection);
    }

    @Test
    void testGetStockByProductId_found_returnsDto() {
        Stock stock = mock(Stock.class);
        StockDto dto = mock(StockDto.class);

        when(stockRepository.findStockByProductId("prod123")).thenReturn(Optional.of(stock));

        // Hier nur die Methode des bereits gemockten Mappers konfigurieren:
        when(stockMapper.stockToDto(stock)).thenReturn(dto);

        // Wenn dein Service den gemockten stockMapper benutzt, dann:
        Optional<StockDto> result = stockService.getStockByProductId("prod123");

        assertTrue(result.isPresent());
    }


    @Test
    void testGetAllStock_returnsMappedDtos() {
        Stock s1 = mock(Stock.class);
        StockDto d1 = mock(StockDto.class);

        when(stockRepository.findAll()).thenReturn(List.of(s1));
        when(stockMapper.stockToDto(s1)).thenReturn(d1);

        List<StockDto> result = stockService.getAllStock();

        assertEquals(1, result.size());
    }


        @Test
        void testAddStock_newProduct_savesStock() throws Exception {
            StockDto dto = mock(StockDto.class);
            when(dto.getProductId()).thenReturn("prod123");

            when(stockRepository.findStockByProductId("prod123")).thenReturn(Optional.empty());
            when(stripeConnection.getStripeClient()).thenReturn(stripeClient); // entsprechend mocken
            when(stripeClient.products().retrieve("prod123")).thenReturn(stripeProduct); // mock

            Stock stock = mock(Stock.class);
            when(stockMapper.stockDtoToStock(dto)).thenReturn(stock);

            Optional<String> result = stockService.addStock(dto);

            assertTrue(result.isEmpty());
        }


        @Test
        void testUpdateStock_existingProduct_updatesFields() {
            StockDto dto = mock(StockDto.class);
            when(dto.getProductId()).thenReturn("prod123");
            when(dto.getQuantity()).thenReturn(5);
            when(dto.isRepurchased()).thenReturn(true);
            when(dto.isShouldBeRepurchased()).thenReturn(false);

            Stock stock = mock(Stock.class);
            when(stockRepository.findStockByProductId("prod123")).thenReturn(Optional.of(stock));

            Optional<String> result = stockService.updateStock(dto);

            assertTrue(result.isEmpty());
            verify(stock).setQuantity(5);
            verify(stock).setRepurchased(true);
            verify(stock).setShouldBeRepurchased(false);
            verify(stockRepository).save(stock);
        }


        @Test
        void testDeleteStock_inactiveProduct_deletesStock() throws Exception {

            // Setup stripeClient to return mocked ProductService
            when(stripeClient.products()).thenReturn(productService);

            // Setup mocked Stock and Stripe Product
            Stock stock = mock(Stock.class);
            when(stockRepository.findStockByProductId("prod123")).thenReturn(Optional.of(stock));
            when(productService.retrieve("prod123")).thenReturn(stripeProduct);
            when(stripeProduct.getActive()).thenReturn(false);

            // Run test
            Optional<String> result = stockService.deleteStock("prod123");

            // Assert and verify
            assertTrue(result.isEmpty());
            verify(stockRepository).delete(stock);
        }


        @Test
        void testProductIsInactive_returnsTrueWhenNotActive() throws Exception {

            // Stelle sicher, dass stripeClient.products() den gemockten Service zurückgibt
            when(stripeClient.products()).thenReturn(productService);

            // Mock Produkt aus Stripe
            when(productService.retrieve("prod123")).thenReturn(stripeProduct);
            when(stripeProduct.getActive()).thenReturn(false);

            // Test durchführen
            boolean result = stockService.productIsInactive("prod123");

            assertTrue(result);
        }


    }
