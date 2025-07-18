package ip.project.backend.backend.repository;

import ip.project.backend.backend.model.Checkout;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutRepository extends MongoRepository<Checkout, ObjectId> {

}
