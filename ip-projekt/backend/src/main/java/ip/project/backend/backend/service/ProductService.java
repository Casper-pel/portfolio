package ip.project.backend.backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.model.checkout.SessionCollection;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionListLineItemsParams;
import com.stripe.param.checkout.SessionListParams;
import ip.project.backend.backend.model.Checkout;
import ip.project.backend.backend.model.Order;
import ip.project.backend.backend.model.ProductWithId;
import ip.project.backend.backend.modeldto.PriceHistoryDto;
import ip.project.backend.backend.modeldto.ProductDto;
import ip.project.backend.backend.modeldto.ProductPriceDto;
import ip.project.backend.backend.modeldto.RespBestSellingProductDto;
import ip.project.backend.backend.repository.OrderRepository;
import ip.project.backend.backend.util.StripeConnection;
import ip.project.backend.backend.util.StripeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ProductService {

    private final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final StripeConnection stripeConnection;
    private final OrderRepository orderRepository;

    @Autowired
    public ProductService(StripeConnection stripeConnection, OrderRepository orderRepository) {
        this.stripeConnection = stripeConnection;
        this.orderRepository = orderRepository;
    }


    /**
     * returns the price id, price and the productId wihtin an optional object
     *
     * @param productId the productId to search for
     * @return returns Optional<ProductPriceDto> with the price id, productId and price
     */
    public Optional<ProductPriceDto> getPriceToProduct(String productId) {
        ProductPriceDto productPriceDto = null;
        try {
            Product product = stripeConnection.getStripeClient().products().retrieve(productId);
            if (product == null) {
                logger.error("Product not found for ID: {}", productId);
                return Optional.empty();
            }
            String priceId = product.getDefaultPrice();
            if (priceId == null) {
                logger.error("No default price found for product ID: {}", productId);
                return Optional.empty();
            }
            Price price = stripeConnection.getStripeClient().prices().retrieve(priceId);
            if (price == null || price.getId() == null) {
                logger.error("Price not found for ID: {}", priceId);
                return Optional.empty();
            }
            productPriceDto = new ProductPriceDto(
                    price.getId(),
                    product.getId(),
                    new BigDecimal(price.getUnitAmount()).divide(new BigDecimal(100), MathContext.DECIMAL128).toBigInteger().floatValue()
            );
        } catch (StripeException e) {
            logger.error("Error retrieving price for product ID {}: {}", productId, e.getMessage());
            return Optional.empty();
        }
        return Optional.of(productPriceDto);
    }


    /**
     * return all products from Stripe
     *
     * @return list of ProductDto objects
     */
    public List<ProductDto> getAllProducts() {
        List<ProductDto> productDtos = new ArrayList<>();

        StripeCollection<Product> stripeProducts;
        try {
            ProductListParams listParams = ProductListParams.builder()
                    .setLimit(100L) // Limit auf 100 Produkte
                    .build();

            stripeProducts = stripeConnection.getStripeClient().products().list(listParams);
        } catch (StripeException e) {
            logger.error("Failed to fetch products: {}", e.getMessage());
            return productDtos;
        }

        if (stripeProducts == null || stripeProducts.getData() == null || stripeProducts.getData().isEmpty()) {
            logger.warn("No products found in Stripe.");
            return productDtos;
        }

        productDtos = stripeProducts.getData().parallelStream()
                .map(this::mapStripeProductToProductDto)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return productDtos;
    }

    /**
     * get all active products from stripe
     *
     * @return retruns list of all active products
     */
    public List<ProductDto> getActiveProducts() {
        List<ProductDto> productDtos = new ArrayList<>();

        ProductListParams params = ProductListParams.builder()
                .setActive(true)  // Nur aktive Produkte
                .setLimit(100L)
                .build();

        StripeCollection<com.stripe.model.Product> stripeProducts;
        try {
            stripeProducts = stripeConnection.getStripeClient().products().list(params);
        } catch (StripeException e) {
            logger.error("Failed to fetch active products: {}", e.getMessage());
            return productDtos;
        }

        if (stripeProducts == null || stripeProducts.getData().isEmpty()) {
            logger.warn("No active products found in Stripe.");
            return productDtos;
        }

        productDtos = stripeProducts.getData().parallelStream()
                .map(this::mapStripeProductToProductDto)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return productDtos;
    }

    /**
     * get product by productId
     *
     * @param productId product id to look for
     * @return returns product dto object
     */
    public Optional<ProductDto> getProductById(String productId) {
        com.stripe.model.Product stripeProduct;
        try {
            stripeProduct = stripeConnection.getStripeClient().products().retrieve(productId);
        } catch (StripeException e) {
            logger.error("error getting product by id: {}", e.getMessage());
            return Optional.empty();
        }

        Optional<ProductDto> productDto = mapStripeProductToProductDto(stripeProduct);
        if (productDto.isEmpty()) {
            logger.error("Product could not be mapped");
            return Optional.empty();
        }
        return productDto;
    }


    /**
     * add new product to database
     *
     * @param productDto new product dto
     * @return returns empty option or error message if not added
     */
    public Optional<String> addProduct(ProductDto productDto) {
        // Optimierung: Überprüfe UPC-Code effizienter (z. B. mit Cache oder Filter)
        if (!checkIfUpCodeInUse(productDto.getUpcCode())) {
            return Optional.of(String.format("UpCode Already in use. Requested UpcCode: %s", productDto.getUpcCode()));
        }

        ProductCreateParams.DefaultPriceData defaultPriceData = StripeUtils.createDefaultPriceData(
                productDto.getCurrency(),
                productDto.getListPrice().multiply(new BigDecimal(100)).longValue(),
                productDto.isTaxIncludedInPrice() ? ProductCreateParams.DefaultPriceData.TaxBehavior.INCLUSIVE : ProductCreateParams.DefaultPriceData.TaxBehavior.EXCLUSIVE,
                productDto.getCostPrice().toString()
        );

        ProductCreateParams params = StripeUtils.createProductParams(
                productDto.getProductName(),
                productDto.getProductDescription(),
                productDto.isActive(),
                StripeUtils.createMetadata(productDto),
                defaultPriceData
        );

        try {
            Product p = stripeConnection.getStripeClient().products().create(params);
            return Optional.of(p.getId());
        } catch (StripeException e) {
            logger.error("Error adding product to Stripe: {}", e.getMessage());
            // Verbesserung: Mehr Kontext zur Fehlermeldung loggen
            return Optional.of("Error adding product to stripe: " + e.getMessage());
        }
    }

    /**
     * update product by productId
     *
     * @param productDto product dto to update
     * @return returns empty option or error message if not updated
     */
    public Optional<String> updateProduct(ProductDto productDto) {
        Product stripeProduct = retrieveStripeProduct(productDto.getProductId());
        if (stripeProduct == null) {
            return Optional.of("Failed to retrieve Stripe product.");
        }

        String oldPriceId = stripeProduct.getDefaultPrice();
        String newPriceId = createNewPrice(productDto, stripeProduct.getId());
        if (newPriceId == null) {
            return Optional.of("Failed to create new price for product.");
        }

        if (!updateStripeProduct(stripeProduct, productDto, newPriceId)) {
            return Optional.of("Failed to update Stripe product.");
        }

        deactivateOldPrice(oldPriceId);
        return Optional.empty();
    }

    /**
     * retrieve product from stripe by productId
     *
     * @param productId productId to retrieve
     * @return returns Product object or null if not found
     */
    Product retrieveStripeProduct(String productId) {
        try {
            return stripeConnection.getStripeClient().products().retrieve(productId);
        } catch (StripeException e) {
            logger.error("Error retrieving product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    /**
     * create a new price for product
     *
     * @param dto       ProductDto object which holds all information about product
     * @param productId productId to create price for
     * @return returns priceId if created successfully, null if not
     */
    String createNewPrice(ProductDto dto, String productId) {
        HashMap<String, String> metadata = new HashMap<>();
        metadata.put("costPrice", dto.getCostPrice().toString());
        PriceCreateParams priceParams = StripeUtils.createPriceParams(
                dto.getCurrency(),
                dto.getListPrice().multiply(BigDecimal.valueOf(100)).longValue(),
                productId,
                dto.isTaxIncludedInPrice() ? PriceCreateParams.TaxBehavior.INCLUSIVE : PriceCreateParams.TaxBehavior.EXCLUSIVE,
                metadata
        );

        try {
            Price newPrice = stripeConnection.getStripeClient().prices().create(priceParams);
            return newPrice != null ? newPrice.getId() : null;
        } catch (StripeException e) {
            logger.error("Error creating price for product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    boolean updateStripeProduct(Product stripeProduct, ProductDto dto, String newPriceId) {
        ProductUpdateParams updateParams = StripeUtils.updateProductParams(
                dto.getProductName(),
                dto.getProductDescription(),
                dto.isActive(),
                StripeUtils.createMetadata(dto),
                newPriceId
        );

        try {
            stripeProduct.update(updateParams);
            return true;
        } catch (StripeException e) {
            logger.error("Error updating product {}: {}", dto.getProductId(), e.getMessage());
            return false;
        }
    }


    /**
     * when updating product, set old price to inactive
     * acts as helper method to updateProduct
     *
     * @param priceId old price id to set to inactive
     */
    void deactivateOldPrice(String priceId) {
        if (priceId == null) return;

        try {
            Price oldPrice = stripeConnection.getStripeClient().prices().retrieve(priceId);
            if (oldPrice == null) {
                logger.warn("Old price {} not found, cannot deactivate.", priceId);
                return;
            }

            oldPrice.update(PriceUpdateParams.builder().setActive(false).build());
            logger.info("Old price {} set to inactive.", priceId);
        } catch (StripeException e) {
            logger.error("Error deactivating old price {}: {}", priceId, e.getMessage());
        }
    }

    /**
     * delete product by productId
     *
     * @param productId product id to delete
     * @return returns empty option or error message if not deleted
     */
    public Optional<String> deleteProduct(String productId) {
        Product stripeProduct;
        try {
            stripeProduct = stripeConnection.getStripeClient().products().retrieve(productId);
        } catch (StripeException e) {
            logger.error("Could not retrieve product to delete: {}", e.getMessage());
            return Optional.of("Could not retrieve product to delete: " + e.getMessage());
        }

        ProductUpdateParams updateParams = ProductUpdateParams.builder()
                .setActive(false)
                .build();

        try {
            stripeProduct.setActive(false);
            stripeProduct = stripeProduct.update(updateParams);
            stripeConnection.getStripeClient().products().update(stripeProduct.getId());
            logger.info("Product {} deleted successfully.", productId);
        } catch (StripeException e) {
            logger.error("Error deleting product: {}", e.getMessage());
            return Optional.of("Error deleting product: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * check if upcCode is already in use
     *
     * @param upcCode upcCode to check
     * @return returns true if not in use and false if in use
     */

    boolean checkIfUpCodeInUse(String upcCode) {
        // Verbesserung: API-Abfrage effizienter gestalten (z. B. nur relevante Felder abfragen)
        StripeCollection<com.stripe.model.Product> stripeProducts;

        try {
            stripeProducts = stripeConnection.getStripeClient().products().list();
        } catch (StripeException e) {
            logger.error("Error fetching products to check UPC code: {}", e.getMessage());
            return false;  // Optimierung: Fehlerbehandlung verbessern, z. B. mit Retry-Logik
        }

        if (stripeProducts == null || stripeProducts.getData().isEmpty()) {
            logger.warn("No products found to check UPC code");
            return true;  // Falls keine Produkte vorhanden sind, wird der UPC-Code als verfügbar betrachtet
        }

        // Verbesserte Suche: Produkte ohne vollständige Metadaten durchsuchen
        return stripeProducts.getData().stream()
                .noneMatch(product -> upcCode.equals(product.getMetadata().get("upcCode")));
    }

    /**
     * get the price history of product with specified productId
     *
     * @param productId productId to search product
     * @return returns PriceHistoryDto object which tells every price an object had and on which date it changed
     */
    public Optional<List<PriceHistoryDto>> getPriceHistory(String productId) {
        Product stripeProduct;
        try { // check if productId is correct
            stripeProduct = stripeConnection.getStripeClient().products().retrieve(productId);
        } catch (StripeException e) {
            logger.error("Could not retrieve product for price history: {}", e.getMessage());
            return Optional.empty();
        }
        if (stripeProduct == null) {
            return Optional.empty();
        }

        PriceSearchParams params = PriceSearchParams.builder()
                .setQuery("product: '" + productId + "'")
                .setLimit(100L)
                .build();

        StripeSearchResult<Price> res = null;
        try {
            res = stripeConnection.getStripeClient().prices().search(params);
        } catch (StripeException e) {
            logger.error("Error searching price history: {}", e.getMessage());
        }

        if (res == null) {
            return Optional.empty();
        }
        List<PriceHistoryDto> list = createPriceList(res.getData());
        return Optional.of(list);
    }

    /**
     * creates a list of PriceHistoryDto objects from a list of individual prices
     *
     * @param individualPrices list of individual prices to create PriceHistoryDto objects from
     * @return returns List of PriceHistoryDto objects
     */
    List<PriceHistoryDto> createPriceList(List<Price> individualPrices) {
        List<PriceHistoryDto> priceHistories = new ArrayList<>();
        for (Price price : individualPrices) {
            Boolean active = price.getActive();
            String currency = price.getCurrency();
            String costPrice = price.getMetadata().get("costPrice");
            Long unitAmount = price.getUnitAmount();
            Date date = new Date(price.getCreated() * 1000);
            if (active == null || currency == null || costPrice == null || unitAmount == null) {
                logger.error("Some values were null. Price: {}, Currency: {}, CostPrice {}, UnitAmount: {}, Date: {}", price, currency, costPrice, unitAmount, date);
            } else {
                priceHistories.add(new PriceHistoryDto(new BigDecimal(unitAmount).divide(new BigDecimal(100)), new BigDecimal(costPrice), date, active, currency));
            }
        }
        Collections.reverse(priceHistories);
        return priceHistories;
    }


    /**
     * return last inactive price for product
     *
     * @param product product to get last inactive price for
     * @return returns last price found if no defaultPrice is found
     */
    Optional<Price> getLastInactivePriceForProduct(Product product) {
        PriceSearchParams searchParams = StripeUtils.createPriceSearchParams(product.getId());
        StripeSearchResult<Price> priceSearchResult;

        try {
            priceSearchResult = stripeConnection.getStripeClient().prices().search(searchParams);
        } catch (StripeException e) {
            logger.error("Error searching price: {}", e.getMessage());
            return Optional.empty();
        }

        return priceSearchResult.getData().stream()
                .filter(price -> !price.getActive()) // Nur inaktive Preise
                .max(Comparator.comparing(Price::getCreated)); // Neuester Preis
    }


    /**
     * Mapper function to convert Stripe Product to ProductDto.
     * This method ensures that null values are handled properly.
     *
     * @param product Stripe product object
     * @return returns Optional<ProductDto>. If an error occurs, it returns an empty optional.
     */
    Optional<ProductDto> mapStripeProductToProductDto(Product product) {
        // Parallel die Preisabfrage verarbeiten
        Optional<Price> priceOpt = fetchPriceForProduct(product);
        if (priceOpt.isEmpty()) {
            return Optional.empty(); // Schnell abbrechen, falls kein Preis gefunden
        }

        Price price = priceOpt.get();

        // Parallel die Verarbeitung von unitAmount und costPrice
        BigDecimal unitAmount = computeUnitAmount(price);
        BigDecimal costPrice = computeCostPrice(price, product);

        boolean isTaxIncluded = "inclusive".equals(price.getTaxBehavior());

        return Optional.of(new ProductDto(
                product.getName(),
                product.getId(),
                product.getDescription(),
                unitAmount,
                costPrice,
                product.getMetadata().get("upcCode"),
                product.getCreated(),
                product.getUpdated(),
                product.getActive(),
                price.getCurrency(),
                isTaxIncluded,
                product.getDefaultPrice()
        ));
    }

    Optional<Price> fetchPriceForProduct(com.stripe.model.Product product) {
        try {
            if (product.getDefaultPrice() == null) {
                return getLastInactivePriceForProduct(product);
            } else {
                return Optional.ofNullable(
                        stripeConnection.getStripeClient().prices().retrieve(product.getDefaultPrice())
                );
            }
        } catch (StripeException e) {
            logger.error("Error retrieving price for product {}: {}", product.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Computes the unit amount from a Stripe Price object.
     *
     * @param price the Stripe Price object
     * @return the unit amount as BigDecimal, divided by 100 to convert from cents to dollars
     */
    BigDecimal computeUnitAmount(Price price) {
        return Optional.ofNullable(price.getUnitAmount())
                .map(val -> new BigDecimal(val).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128))
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Computes the cost price from a Stripe Price object.
     *
     * @param price   the Stripe Price object
     * @param product the Stripe Product object
     * @return the cost price as BigDecimal, or BigDecimal.ZERO if not found or invalid
     */
    BigDecimal computeCostPrice(Price price, com.stripe.model.Product product) {
        return Optional.ofNullable(price.getMetadata())
                .map(meta -> meta.get("costPrice"))
                .map(priceStr -> {
                    try {
                        return new BigDecimal(priceStr);
                    } catch (NumberFormatException e) {
                        logger.error("Invalid cost price format for product {}: {}", product.getId(), e.getMessage());
                        return BigDecimal.ZERO;
                    }
                })
                .orElseGet(() -> {
                    logger.warn("Product {} has no cost price metadata", product.getId());
                    return BigDecimal.ZERO;
                });
    }


    /**
     * get the best selling product within a specified date range
     *
     * @param startDate start date to search for
     * @param endDate   end date to search for
     * @return returns RespBestSellingProductDto object which holds the best selling product, its sale dates and total quantity sold
     */
    public RespBestSellingProductDto getBestSellingProduct(Date startDate, Date endDate) {
        SessionListParams sessionParams = SessionListParams.builder()
                .setLimit(100L)
                .build();

        Map<String, Long> productQuantities = new ConcurrentHashMap<>();
        Map<String, List<Date>> productSaleDates = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(10); // 10 Threads für parallele Stripe-Requests
        List<Future<?>> futures = new ArrayList<>();

        try {
            StripeCollection<Session> sessions = stripeConnection.getStripeClient()
                    .checkout()
                    .sessions()
                    .list(sessionParams);

            for (Session session : sessions.getData()) {
                // Filter by date range
                long created = session.getCreated() * 1000L;
                Date sessionDate = new Date(created);
                if (sessionDate.before(startDate) || sessionDate.after(endDate)) {
                    continue;
                }

                // Check if payment succeeded
                if (!"paid".equals(session.getPaymentStatus())) {
                    continue; // skip unpaid or incomplete payments
                }

                // Parallel task for line items
                futures.add(executor.submit(() -> {
                    try {
                        SessionListLineItemsParams lineItemParams = SessionListLineItemsParams.builder()
                                .setLimit(100L)
                                .build();

                        LineItemCollection lineItems = session.listLineItems(lineItemParams);

                        for (LineItem item : lineItems.getData()) {
                            if (item.getPrice() != null && item.getPrice().getProduct() != null) {
                                String productId = item.getPrice().getProduct();
                                long quantity = item.getQuantity() != null ? item.getQuantity() : 0L;

                                productQuantities.merge(productId, quantity, Long::sum);
                                productSaleDates.computeIfAbsent(productId, k -> Collections.synchronizedList(new ArrayList<>()))
                                        .add(sessionDate);
                            }
                        }
                    } catch (StripeException e) {
                        logger.error("Error retrieving line items for session {}: {}", session.getId(), e.getMessage());
                    }
                }));
            }


            // Warten bis alle Stripe-Tasks fertig sind
            for (Future<?> future : futures) {
                future.get();
            }

        } catch (StripeException | InterruptedException | ExecutionException e) {
            logger.error("Error fetching checkout sessions: {}", e.getMessage());
            Thread.currentThread().interrupt(); // falls interrupted
            return null;
        } finally {
            executor.shutdown();
        }

        // Bestverkauftes Produkt ermitteln
        Map.Entry<String, Long> bestEntry = productQuantities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (bestEntry == null) {
            return null;
        }

        String bestProductId = bestEntry.getKey();
        int totalQuantity = bestEntry.getValue().intValue();
        List<Date> saleDates = productSaleDates.getOrDefault(bestProductId, new ArrayList<>());

        // Produktnamen (optional via Stripe, oder aus eigener DB)
        String productName = getNameToProductId(bestProductId); // eigene Hilfsmethode

        return new RespBestSellingProductDto(productName, bestProductId, saleDates, totalQuantity);
    }


    /**
     * get the name of a product by its productId
     *
     * @param productId productId to search for
     * @return returns the name of the product or null if not found
     */
    String getNameToProductId(String productId) {
        try {
            Product product = stripeConnection.getStripeClient().products().retrieve(productId);
            if (product != null) {
                return product.getName();
            }
        } catch (StripeException e) {
            logger.error("Error retrieving product name for ID {}: {}", productId, e.getMessage());
        }
        return null;
    }


}
