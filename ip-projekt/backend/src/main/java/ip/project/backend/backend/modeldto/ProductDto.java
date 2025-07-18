package ip.project.backend.backend.modeldto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ProductDto {

    @NotNull
    @NotBlank(message = "Product name is mandatory")
    private String productName;

    @NotNull
    private String productId;

    @NotNull
    @NotBlank(message = "Product description is mandatory")
    private String productDescription;

    @NotNull
    @Digits(integer = 10, fraction = 2)
    private BigDecimal listPrice; // price when selling

    @NotNull
    @Digits(integer = 10, fraction = 2)
    private BigDecimal costPrice; // price when buying

    @NotNull
    @NotBlank(message = "UPC code is mandatory")
    private String upcCode;

    @NotNull
    private Long created;

    @NotNull
    private Long updated;

    @NotNull
    private boolean active;

    @NotNull
    @NotBlank
    private String currency;

    @NotNull
    private boolean taxIncludedInPrice; // true if tax is included in price or false if not included

    @NotNull
    private String priceId;


    public ProductDto(String productName, String productId, String productDescription, BigDecimal listPrice, BigDecimal costPrice, String upcCode, Long created, Long updated, boolean active, String currency, boolean taxIncludedInPrice, String priceId) {
        this.productName = productName;
        this.productId = productId;
        this.productDescription = productDescription;
        this.listPrice = listPrice;
        this.costPrice = costPrice;
        this.upcCode = upcCode;
        this.created = created;
        this.updated = updated;
        this.active = active;
        this.currency = currency;
        this.taxIncludedInPrice = taxIncludedInPrice;
        this.priceId = priceId;
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

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public BigDecimal getListPrice() {
        return listPrice;
    }

    public void setListPrice(BigDecimal listPrice) {
        this.listPrice = listPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public String getUpcCode() {
        return upcCode;
    }

    public void setUpcCode(String upcCode) {
        this.upcCode = upcCode;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isTaxIncludedInPrice() {
        return taxIncludedInPrice;
    }

    public void setTaxIncludedInPrice(boolean taxIncludedInPrice) {
        this.taxIncludedInPrice = taxIncludedInPrice;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }
}
