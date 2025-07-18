package ip.project.backend.backend.repository;

import ip.project.backend.backend.model.Order;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

//@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findOrderByOrderId(String orderId);
    List<Order> getByDateBetween(Date start, Date end);
    List<Order> getByDateBetweenAndEmployeeId(Date start, Date end, Integer employeeId);

}
