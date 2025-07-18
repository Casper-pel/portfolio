package ip.project.backend.backend.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import ip.project.backend.backend.mapper.StockMapper;
import ip.project.backend.backend.model.Stock;
import ip.project.backend.backend.modeldto.StockDto;
import ip.project.backend.backend.repository.StockRepository;
import ip.project.backend.backend.util.StripeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockService {
    private final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final StockRepository stockRepository;
    private final StripeConnection stripeConnection;

    @Autowired
    public StockService(StockRepository stockRepository, StripeConnection stripeConnection) {
        this.stockRepository = stockRepository;
        this.stripeConnection = stripeConnection;
    }


    /**
     * get Stock by product id
     *
     * @param productId product id
     * @return returns Stock Object
     */
    public Optional<StockDto> getStockByProductId(String productId) {
        Optional<Stock> stock = stockRepository.findStockByProductId(productId);
        if (stock.isEmpty()) {
            logger.error("Stock not found with product id: {}", productId);
            return Optional.empty();
        } else {
            return Optional.of(StockMapper.INSTANCE.stockToDto(stock.get()));
        }
    }


    /**
     * get all stock
     *
     * @return returns list of StockDtos
     */
    public List<StockDto> getAllStock() {
        List<Stock> stocks = stockRepository.findAll();
        List<StockDto> stockDtos = new ArrayList<>();
        if (stocks.isEmpty()) return stockDtos;

        for (Stock stock : stocks) {
            stockDtos.add(StockMapper.INSTANCE.stockToDto(stock));
        }
        return stockDtos;
    }

    /**
     * adds stock to database
     *
     * @param stockDto StockDto object to add
     * @return returns empty optional or error message in optional object
     */
    public Optional<String> addStock(StockDto stockDto) {
        if(stockRepository.findStockByProductId(stockDto.getProductId()).isPresent()) {
            logger.error("Stock already exists with product id: {}", stockDto.getProductId());
            return Optional.of("Stock already exists with product id: " + stockDto.getProductId());
        }
        Product product = null;
        try {
            product = stripeConnection.getStripeClient().products().retrieve(stockDto.getProductId());
        } catch (StripeException e) {
            logger.error("Error retrieving product from Stripe: {}", e.getMessage());
        }
        if (product == null) {
            logger.error("Product not found with id: {}", stockDto.getProductId());
            return Optional.of("Product not found with id: " + stockDto.getProductId());
        }

        Stock stock = StockMapper.INSTANCE.stockDtoToStock(stockDto);
        stockRepository.save(stock);
        return Optional.empty();
    }

    /**
     * updates stock in database
     *
     * @param stockDto StockDto object to update
     * @return returns empty optional or error message in optional object
     */
    public Optional<String> updateStock(StockDto stockDto) {
        Optional<Stock> stock = stockRepository.findStockByProductId(stockDto.getProductId());
        if (stock.isEmpty()) {
            Stock s = new Stock(
                    null,
                    stockDto.getProductId(),
                    stockDto.getQuantity(),
                    stockDto.isRepurchased(),
                    stockDto.isShouldBeRepurchased()
            );
            stockRepository.insert(s);
            return Optional.empty();
        }
        stock.get().setQuantity(stockDto.getQuantity());
        stock.get().setRepurchased(stockDto.isRepurchased());
        stock.get().setShouldBeRepurchased(stockDto.isShouldBeRepurchased());
        stockRepository.save(stock.get());
        return Optional.empty();
    }


    /**
     * deletes stock from database
     * also checks if product is still in listed in other productRepo. If so stock cannot be deleted
     *
     * @param productId product id of given stock
     * @return returns Error message in optional object or empty optional if successful
     */
    public Optional<String> deleteStock(String productId) {
        Optional<Stock> stock = stockRepository.findStockByProductId(productId);
        if (stock.isEmpty()) {
            logger.error("Stock to delete not found with product id: {}", productId);
            return Optional.of("Stock not found with product id: " + productId);
        }
        if (!productIsInactive(productId)) {
            logger.error("Product is still active. Cannot delete stock");
            return Optional.of("Product is still active. Cannot delete stock");
        }

        stockRepository.delete(stock.get());
        return Optional.empty();
    }


    /**
     * stock can only be removed if product is inactive
     *
     * @param productId product id of given stock
     * @return returns true if product is inactive, false otherwise
     */
    boolean productIsInactive(String productId) {
        Product product = null;
        try {
            product = stripeConnection.getStripeClient().products().retrieve(productId);
        } catch (StripeException e) {
            logger.error("Error retrieving product to check for inactivity from Stripe: {}", e.getMessage());
        }
        if (product == null) {
            logger.error("Product to check for inactivity not found with id: {}", productId);
            return false;
        }
        return !product.getActive();
    }

}
