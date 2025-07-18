package ip.project.backend.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ip.project.backend.backend.modeldto.StockDto;
import ip.project.backend.backend.service.StockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock")
@EnableCaching
public class StockController {
    private final Logger logger = LoggerFactory.getLogger(StockController.class);
    private final StockService stockService;


    /**
     * basic get mapping for getting stock by product id
     *
     * @param productId product id to get stock
     * @return returns StockDto object
     */
    @Operation(summary = "Get stock by product id", description = "Get stock by product id")
    @ApiResponse(responseCode = "200", description = "Stock found. Returning stock as StockDto Object")
    @ApiResponse(responseCode = "204", description = "No Stock found")

    @GetMapping("/{id}")
    public ResponseEntity<StockDto> getStock(@Parameter(description = "ProductID to identify specific stock", required = true) @NotNull @PathVariable("id") String productId) {

        Optional<StockDto> stock = stockService.getStockByProductId(productId);

        if (stock.isEmpty()) {
            logger.info("Stock not found for product id: {}", productId);
            return ResponseEntity.noContent().build();
        } else {
            logger.info("Stock found for product id: {}", productId);
            return ResponseEntity.ok(stock.get());
        }
    }

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * get all stock objects from database
     *
     * @return returns list of StockDto objects
     */
    @Operation(summary = "Get all Stock", description = "Get all stock from the database. Returns list of StockDto objects")
    @ApiResponse(responseCode = "200", description = "Stock found and returned. No Success Message will be provided")
    @ApiResponse(responseCode = "204", description = "No stock found. Error message provided")

    @GetMapping("/all")
    @Cacheable("AllStock")
    public ResponseEntity<List<StockDto>> getAllStock() {
        List<StockDto> stock = stockService.getAllStock();
        if (stock.isEmpty()) {
            logger.info("No stock found");
            return ResponseEntity.noContent().build();
        } else {
            logger.info("Stock found. Returning all");
            return ResponseEntity.ok(stock);
        }
    }

    /**
     * add new stock to existing product database
     *
     * @param stockDto StockDto object to add
     * @return returns 400 and error message or just 200
     */
    @Operation(summary = "Add stock", description = "Add new stock to existing product")
    @ApiResponse(responseCode = "200", description = "Stock added successfully")
    @ApiResponse(responseCode = "400", description = "Stock not added. Error message provided")

    @PostMapping("/add")
    @CacheEvict(value = "AllStock", allEntries = true)
    public ResponseEntity<String> addStock(@Parameter(description = "StockDto Object to add into the databse", required = true) @NotNull @Valid @RequestBody StockDto stockDto) {

        Optional<String> answer = stockService.addStock(stockDto);

        if (answer.isEmpty()) {
            logger.info("Stock added successfully");
            return ResponseEntity.ok("Stock added successfully");
        } else {
            logger.error("Stock not added");
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    /**
     * update stock in database
     *
     * @param stockDto StockDto object to update
     * @return returns 400 and error message or just code 200
     */
    @Operation(summary = "Stock updates", description = "Update stock in database")
    @ApiResponse(responseCode = "200", description = "Stock updated successfully")
    @ApiResponse(responseCode = "400", description = "Stock not updated. Error message provided")

    @PutMapping("/update")
    @CacheEvict(value = "AllStock", key = "#stockDto.productId")
    public ResponseEntity<String> updateStock(@Parameter(description = "updated Stock object", required = true) @Valid @NotNull @RequestBody StockDto stockDto) {

        Optional<String> answer = stockService.updateStock(stockDto);

        if (answer.isEmpty()) {
            logger.info("Stock updated successfully");
            return ResponseEntity.ok("Stock updated successfully");
        } else {
            logger.error("Stock not updated");
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Delete stock", description = "Delete stock from database")
    @ApiResponse(responseCode = "200", description = "Stock deleted successfully")
    @ApiResponse(responseCode = "400", description = "Stock not deleted. Error message provided")

    @DeleteMapping("/delete/{id}")
    @CacheEvict(value = "AllStock", key = "#productId")
    public ResponseEntity<String> deleteStock(@Parameter(description = "ProductID of the stock to delete", required = true, example = "1") @NotNull @PathVariable("id") String productId) {
        Optional<String> response = stockService.deleteStock(productId);
        if (response.isEmpty()) {
            logger.info("Stock deleted successfully");
            return ResponseEntity.ok("Stock deleted successfully");
        } else {
            logger.error("Error deleting stock: {}", response.get());
            return ResponseEntity.badRequest().body(response.get());
        }
    }

}
