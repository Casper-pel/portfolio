package ip.project.backend.backend.modeldto;

public class ProductPriceDto {

    String priceId;
    String productId;
    Float price;

    public ProductPriceDto(String priceId, String productId, Float price) {
        this.priceId = priceId;
        this.productId = productId;
        this.price = price;
    }


    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }



}
