package ip.project.backend.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stock")
public class Stock {

    @Id
    private ObjectId _id;
    private String productId;
    private Integer quantity;
    private boolean repurchased;
    private boolean shouldBeRepurchased;

    public Stock(ObjectId _id, String productId, Integer quantity, boolean repurchased, boolean shouldBeRepurchased) {
        this._id = _id;
        this.productId = productId;
        this.quantity = quantity;
        this.repurchased = repurchased;
        this.shouldBeRepurchased = shouldBeRepurchased;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public boolean isRepurchased() {
        return repurchased;
    }

    public void setRepurchased(boolean repurchased) {
        this.repurchased = repurchased;
    }

    public boolean isShouldBeRepurchased() {
        return shouldBeRepurchased;
    }

    public void setShouldBeRepurchased(boolean shouldBeRepurchased) {
        this.shouldBeRepurchased = shouldBeRepurchased;
    }
}
