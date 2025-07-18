package ip.project.backend.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class ProductWithId {

    @Id
    private ObjectId id;

    private String productId;

    private int quantity;


    public ProductWithId(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
