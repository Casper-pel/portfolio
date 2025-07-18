package ip.project.backend.backend.repository;

import ip.project.backend.backend.model.Stock;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends MongoRepository<Stock, ObjectId> {
    Optional<Stock> findStockByProductId(String productId);
}
