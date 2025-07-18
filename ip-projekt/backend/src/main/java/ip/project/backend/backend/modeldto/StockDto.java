package ip.project.backend.backend.modeldto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockDto {

    @NotNull
    private String productId;

    @NotNull
    @Min(0)
    private Integer quantity;

    @NotNull
    private boolean repurchased;

    @NotNull
    private boolean shouldBeRepurchased;

    public StockDto(String productId, Integer quantity, boolean repurchased, boolean shouldBeRepurchased) {
        this.productId = productId;
        this.quantity = quantity;
        this.repurchased = repurchased;
        this.shouldBeRepurchased = shouldBeRepurchased;
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
