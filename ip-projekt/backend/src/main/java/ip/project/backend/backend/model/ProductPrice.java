package ip.project.backend.backend.model;

import com.stripe.model.Price;

public class ProductPrice {

    private String productId;
    private Price price;
    private int quantity;

    public ProductPrice(String productId, Price price, int quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }
}
