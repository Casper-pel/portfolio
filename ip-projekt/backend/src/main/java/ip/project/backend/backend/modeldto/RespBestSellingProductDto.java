package ip.project.backend.backend.modeldto;

import java.util.Date;
import java.util.List;

public class RespBestSellingProductDto {

    private String productName;
    private String productId;
    private List<Date> dates; // on these dates the product was sold
    private int totalQuantity; // total quantity sold

    public RespBestSellingProductDto(String productName, String productId, List<Date> dates, int totalQuantity) {
        this.productName = productName;
        this.productId = productId;
        this.dates = dates;
        this.totalQuantity = totalQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
