package ip.project.backend.backend.modeldto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class NewCouponDto {

    @NotBlank
    private String name;

    @Min(0)
    private Integer amountOff;

    @NotBlank
    private String currency;

    @NotBlank
    private String duration;

    @DecimalMin(value = "0.0", inclusive = true)
    private Float percentOff;


    public NewCouponDto(String name, Integer amountOff, String currency, String duration, Float percentOff) {
        this.name = name;
        this.amountOff = amountOff;
        this.currency = currency;
        this.duration = duration;
        this.percentOff = percentOff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAmountOff() {
        return amountOff;
    }

    public void setAmountOff(Integer amountOff) {
        this.amountOff = amountOff;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Float getPercentOff() {
        return percentOff;
    }

    public void setPercentOff(Float percentOff) {
        this.percentOff = percentOff;
    }
}
