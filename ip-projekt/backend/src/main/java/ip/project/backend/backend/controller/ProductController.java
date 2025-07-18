package ip.project.backend.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ip.project.backend.backend.modeldto.PriceHistoryDto;
import ip.project.backend.backend.modeldto.ProductDto;
import ip.project.backend.backend.modeldto.RespBestSellingProductDto;
import ip.project.backend.backend.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/products")
@EnableCaching
public class ProductController {

    private final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final CacheManager cacheManager;

    @Autowired
    public ProductController(ProductService productService, CacheManager cacheManager) {
        this.productService = productService;
        this.cacheManager = cacheManager;
    }

    @Operation(summary = "Get all Products", description = "Retrieves all products from the database. Returns a list of ProductDto objects.")
    @ApiResponse(responseCode = "200", description = "Products found and returned. No success message provided.")
    @ApiResponse(responseCode = "204", description = "No products found. Error message provided.")
    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        logger.info("Getting all products");
        Cache cache = cacheManager.getCache("AllProducts");

        if (cache == null || cache.get("all") == null) {
            logger.info("AllProducts cache is empty or missing. Loading from service.");
            getAllProductsToCache();
        }

        @SuppressWarnings("unchecked")
        List<ProductDto> cachedProducts = cache.get("all", List.class);
        if (cachedProducts == null || cachedProducts.isEmpty()) {
            logger.info("No products found in cache");
            return ResponseEntity.noContent().build();
        }

        logger.info("Returning products from cache");
        return ResponseEntity.ok(cachedProducts);
    }

    @Operation(summary = "Get specific Product", description = "Retrieves a product by its ProductID. Returns a ProductDto object.")
    @ApiResponse(responseCode = "200", description = "Product found and returned.")
    @ApiResponse(responseCode = "204", description = "Product not found.")
    @GetMapping("/get/{id}")
    public ResponseEntity<ProductDto> getProduct(@Parameter(description = "ProductID Integer", required = true) @NotNull @NotBlank @PathVariable("id") String productId) {
        Cache cache = cacheManager.getCache("AllProducts");

        if (cache == null || cache.get("all") == null) {
            logger.info("AllProducts cache is empty or missing. Loading from service.");
            getAllProductsToCache();
        }

        ProductDto cached = cache.get(productId, ProductDto.class);
        if (cached != null) {
            logger.info("Product found in cache for id {}", productId);
            return ResponseEntity.ok(cached);
        }

        Optional<ProductDto> product = productService.getProductById(productId);
        if (product.isEmpty()) {
            logger.info("Product not found for id {}", productId);
            return ResponseEntity.noContent().build();
        } else {
            if (cache != null) {
                cache.put(productId, product.get());
            }
            return ResponseEntity.ok(product.get());
        }
    }

    @Operation(summary = "Get all active products", description = "Retrieves all products that are marked as active.")
    @ApiResponse(responseCode = "200", description = "Active products found and returned.")
    @ApiResponse(responseCode = "204", description = "No active products found.")
    @ApiResponse(responseCode = "400", description = "An error occurred during the process.")
    @GetMapping("/all/active")
    public ResponseEntity<List<ProductDto>> getActiveProducts() {
        Cache cache = cacheManager.getCache("ActiveProducts");
        if (cache != null) {
            @SuppressWarnings("unchecked")
            List<ProductDto> cached = cache.get("all", List.class);
            if (cached != null) {
                return ResponseEntity.ok(cached);
            }
        }

        List<ProductDto> products = productService.getAllProducts();
        if (products == null) {
            return ResponseEntity.badRequest().build();
        }
        if (products.isEmpty()) {
            logger.info("No active products found");
        }
        if (cache != null) {
            cache.put("all", products);
        }
        return ResponseEntity.ok(products);
    }

    @PostMapping("/add")
    @Operation(summary = "Add Product", description = "Adds a new product to the database.")
    @ApiResponse(responseCode = "200", description = "Product added successfully.")
    @ApiResponse(responseCode = "400", description = "Error adding product. Details are included in the response.")
    public ResponseEntity<String> addProduct(@Parameter(description = "New product in DTO format", required = true) @NotNull @Valid @RequestBody ProductDto productDto) {
        logger.info("Adding product to the database: {}", productDto);
        Optional<String> response = productService.addProduct(productDto);
        if (response.isEmpty()) return ResponseEntity.badRequest().body("Error adding product");

        if (response.get().startsWith("prod_")) {
            logger.info("Product added successfully");

            evictCaches(productDto.getProductId());

            return ResponseEntity.ok(response.get());
        } else {
            logger.error("Error adding product: {}", response.get());
            return ResponseEntity.badRequest().body(response.get());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "Update Product", description = "Updates an existing product by its ID.")
    @ApiResponse(responseCode = "200", description = "Product updated successfully.")
    @ApiResponse(responseCode = "400", description = "Error updating product. Details are included in the response.")
    public ResponseEntity<String> updateProduct(@Parameter(description = "ProductDto object to update", required = true) @NotNull @Valid @RequestBody ProductDto productDto) {
        Optional<String> response = productService.updateProduct(productDto);
        if (response.isEmpty()) {
            logger.info("Product updated successfully");

            evictCaches(productDto.getProductId());

            return ResponseEntity.ok("Product updated successfully");
        } else {
            logger.error("Error updating product: {}", response.get());
            return ResponseEntity.badRequest().body(response.get());
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete Product", description = "Deletes a product by its ID.")
    @ApiResponse(responseCode = "200", description = "Product deleted successfully.")
    @ApiResponse(responseCode = "400", description = "Error deleting product. Possibly not found.")
    public ResponseEntity<String> deleteProduct(@Parameter(description = "ProductID of the product to delete", required = true) @NotNull @PathVariable("id") String productId) {
        Optional<String> response = productService.deleteProduct(productId);
        if (response.isEmpty()) {
            logger.info("Product deleted successfully");

            evictCaches(productId);

            return ResponseEntity.ok("Product deleted successfully");
        } else {
            logger.error("Error deleting product: {}", response.get());
            return ResponseEntity.badRequest().body(response.get());
        }
    }

    @GetMapping("/price-history/{productId}")
    public ResponseEntity<List<PriceHistoryDto>> getPriceHistory(
            @PathVariable @NotNull @NotBlank @Length(min = 10, max = 255) String productId) {

        Cache cache = cacheManager.getCache("PriceHistory");
        if (cache != null) {
            // Try to read from cache
            Cache.ValueWrapper cachedValue = cache.get(productId);
            if (cachedValue != null) {
                @SuppressWarnings("unchecked")
                List<PriceHistoryDto> cachedList = (List<PriceHistoryDto>) cachedValue.get();
                if (cachedList != null && !cachedList.isEmpty()) {
                    return ResponseEntity.ok(cachedList);
                } else if (cachedList != null && cachedList.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
            }
        }

        // Load from service if not found in cache
        Optional<List<PriceHistoryDto>> response = productService.getPriceHistory(productId);
        if (response.isEmpty()) {
            logger.info("Some error occurred while getting price history");
            return ResponseEntity.badRequest().build();
        } else if (response.get().isEmpty()) {
            logger.warn("No prices were found");
            return ResponseEntity.noContent().build();
        } else {
            // Put result into cache
            if (cache != null) {
                cache.put(productId, response.get());
            }
            return ResponseEntity.ok(response.get());
        }
    }

    @Operation(
            summary = "Get product by EAN from cache",
            description = "Searches for a product with the given EAN in the 'AllProducts' cache and returns it if found."
    )
    @ApiResponse(responseCode = "200", description = "Product with given EAN found in cache and returned.")
    @ApiResponse(responseCode = "204", description = "No product found with the given EAN in cache.")
    @GetMapping("/cache/product/ean/{ean}")
    public ResponseEntity<ProductDto> getProductFromCacheByEan(
            @Parameter(description = "EAN of the product", required = true)
            @PathVariable @Length(min = 0, max = 255) String ean) {

        Cache cache = cacheManager.getCache("AllProducts");

        if (cache == null || cache.get("all") == null) {
            logger.info("AllProducts cache is empty or missing. Loading from service.");
            getAllProductsToCache();
        }

        List<ProductDto> cachedProducts = cache.get("all", List.class);
        if (cachedProducts == null || cachedProducts.isEmpty()) {
            cachedProducts = productService.getAllProducts(); // fallback
            cache.put("all", cachedProducts);
        }

        return cachedProducts.stream()
                .filter(p -> ean.equals(p.getUpcCode()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Get best-selling product",
            description = "Retrieves the best-selling product for a given date range."
    )
    @ApiResponse(responseCode = "200", description = "Best-selling product found and returned.")
    @ApiResponse(responseCode = "204", description = "No best-selling product found for the specified time period.")
    @GetMapping("best-selling")
    public ResponseEntity<RespBestSellingProductDto> getBestSellingProduct(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        RespBestSellingProductDto res = productService.getBestSellingProduct(startDate, endDate);
        if (res == null || res.getProductId() == null) {
            logger.warn("No best selling product found for the given time period");
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(res);
        }
    }

    // Loads all products into the cache
    private void getAllProductsToCache() {
        List<ProductDto> productDtos = productService.getAllProducts();
        Cache targetCache = cacheManager.getCache("AllProducts");
        if (targetCache != null) {
            targetCache.put("all", productDtos);
            for (ProductDto product : productDtos) {
                targetCache.put(product.getProductId(), product);
            }
        }
    }

    // Clears relevant caches when product is added, updated or deleted
    private void evictCaches(String productId) {
        Optional.ofNullable(cacheManager.getCache("AllProducts")).ifPresent(cache -> {
            cache.evict(productId);
            cache.evict("all");
        });
        Optional.ofNullable(cacheManager.getCache("AllStock")).ifPresent(cache -> cache.evict(productId));
        Optional.ofNullable(cacheManager.getCache("ActiveProducts")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("PriceToProductId")).ifPresent(Cache::clear);
    }
}
