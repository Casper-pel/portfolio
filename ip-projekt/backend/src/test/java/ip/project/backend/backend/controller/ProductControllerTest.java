package ip.project.backend.backend.controller;
import ip.project.backend.backend.controller.ProductController;
import ip.project.backend.backend.modeldto.ProductDto;
import ip.project.backend.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    private ProductService productService;
    private CacheManager cacheManager;
    private Cache cache;

    private ProductController productController;

    @BeforeEach
    void setup() {
        productService = mock(ProductService.class);
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);

        // CacheManager gibt unseren Cache zurück, wenn "AllProducts" angefragt wird
        when(cacheManager.getCache("AllProducts")).thenReturn(cache);

        productController = new ProductController(productService, cacheManager);
    }
    @Test
    void testGetAllProducts_returnsFromCache() {
        // Dummy-Produkt mocken
        ProductDto dummyProduct = mock(ProductDto.class);
        when(dummyProduct.getProductId()).thenReturn("prod_123");

        List<ProductDto> productList = List.of(dummyProduct);

        // Cache.ValueWrapper mocken
        Cache.ValueWrapper valueWrapper = mock(Cache.ValueWrapper.class);
        when(valueWrapper.get()).thenReturn(productList);

        // Cache mocken
        when(cache.get("all")).thenReturn(valueWrapper);
        when(cache.get("all", List.class)).thenReturn(productList);

        // Methode aufrufen
        ResponseEntity<List<ProductDto>> response = productController.getAllProducts();

        // Assertions
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(productList, response.getBody());

        // Service wurde nicht aufgerufen
        verify(productService, never()).getAllProducts();
    }



    @Test
    void testGetAllProducts_cacheEmpty_loadsFromService() {
        // Cache liefert null -> leer
        when(cache.get("all", List.class)).thenReturn(List.of());
        // Service liefert Produktliste
        ProductDto dummyProduct = mock(ProductDto.class);
        when(dummyProduct.getProductId()).thenReturn("prod_456");
        List<ProductDto> serviceList = List.of(dummyProduct);
        when(productService.getAllProducts()).thenReturn(serviceList);

        // Damit cache.put funktioniert (void Methode)
        doNothing().when(cache).put(eq("all"), any());

        ResponseEntity<List<ProductDto>> response = productController.getAllProducts();

        assertEquals(204, response.getStatusCodeValue());

        verify(productService).getAllProducts();
        verify(cache).put("all", serviceList);
    }

    @Test
    void testGetProduct_returnsProductFromCache() {
        String productId = "prod_789";

        ProductDto cachedProduct = mock(ProductDto.class);
        when(cachedProduct.getProductId()).thenReturn(productId);

        // Cache liefert Produkt zurück
        when(cache.get("all")).thenReturn(null); // so dass er cache neu lädt
        when(cache.get(productId, ProductDto.class)).thenReturn(cachedProduct);

        ResponseEntity<ProductDto> response = productController.getProduct(productId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(cachedProduct, response.getBody());

        // Service nicht aufgerufen
        verify(productService, never()).getProductById(anyString());
    }

    @Test
    void testGetProduct_cacheEmpty_loadsFromService() {
        String productId = "prod_101";

        when(cache.get("all")).thenReturn(null);
        when(cache.get(productId, ProductDto.class)).thenReturn(null);

        ProductDto serviceProduct = mock(ProductDto.class);
        when(serviceProduct.getProductId()).thenReturn(productId);

        when(productService.getProductById(productId)).thenReturn(Optional.of(serviceProduct));

        doNothing().when(cache).put(eq(productId), any());

        ResponseEntity<ProductDto> response = productController.getProduct(productId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(serviceProduct, response.getBody());

        verify(productService).getProductById(productId);
        verify(cache).put(productId, serviceProduct);
    }

    @Test
    void testGetProduct_notFound_returnsNoContent() {
        String productId = "prod_404";

        when(cache.get("all")).thenReturn(null);
        when(cache.get(productId, ProductDto.class)).thenReturn(null);

        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        ResponseEntity<ProductDto> response = productController.getProduct(productId);

        assertEquals(204, response.getStatusCodeValue());

        verify(productService).getProductById(productId);
        verify(cache, never()).put(eq(productId), any());
    }
}
