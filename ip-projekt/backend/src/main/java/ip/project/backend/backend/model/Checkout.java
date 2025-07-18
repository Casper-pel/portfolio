package ip.project.backend.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "checkout")
public class Checkout {

    @Id
    private ObjectId id;

    private String sessionId;

    private Date date;

    private List<ProductWithId> products;

    public Checkout(String sessionId, Date date, List<ProductWithId> products) {
        this.sessionId = sessionId;
        this.date = date;
        this.products = products;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<ProductWithId> getProducts() {
        return products;
    }

    public void setProducts(List<ProductWithId> products) {
        this.products = products;
    }
}
