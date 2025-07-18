package ip.project.backend.backend.modeldto;


import java.math.BigDecimal;
import java.util.Date;

public class PriceHistoryDto {

    private BigDecimal listPrice;
    private BigDecimal costPrice;
    private Date changedDate;
    private boolean active; // true -> active; false -> inactive
    private String currency;

    public PriceHistoryDto(BigDecimal listPrice, BigDecimal costPrice, Date changedDate, boolean isActive, String currency) {
        this.listPrice = listPrice;
        this.costPrice = costPrice;
        this.changedDate = changedDate;
        this.active = isActive;
        this.currency = currency;
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

    public Date getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(Date changedDate) {
        this.changedDate = changedDate;
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
}
