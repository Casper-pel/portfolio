package ip.project.backend.backend.modeldto;

public class CouponDto {
    private String id;
    private String name;
    private Integer amountOff;
    private String currency;
    private String duration;
    private Float percentOff;


    public CouponDto() {}


    public CouponDto(String id, String name, Integer amountOff, String currency, String duration, Float percentOff) {
        this.id = id;
        this.name = name;
        this.amountOff = amountOff;
        this.currency = currency;
        this.duration = duration;
        this.percentOff = percentOff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
