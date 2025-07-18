package ip.project.backend.backend.modeldto;

import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class OrderDto {

//    @Id
//    private ObjectId id;
//    @NotNull(message = "Order Id ist erforderlich")
//    private String orderId;
    @NotNull(message = "Produkt liste ist leer")
    private List<String> productNames;

    @NotNull(message = "Gesamt preis ist erforderlich")
    private BigDecimal totalPrice;

    @NotNull(message = "Datum ist erforderlich")
    private Date date;

    @NotNull(message = "Kassierer Id ist erforderlich")
    private Integer employeeId;

    public OrderDto() {}

    public OrderDto( List<String> productNames, BigDecimal totalPrice, Date date, Integer employeeId){
//        this.orderId = orderId;
        this.productNames = productNames;
        this.totalPrice = totalPrice;
        this.date = date;
        this.employeeId = employeeId;
    }

//    public ObjectId getId(){
//        return id;
//    }

//    public void setId(ObjectId id){this.id = id;}

//    public String getOrderId(){
//        return orderId;
//    }

//    public void setOrderId(String orderId){this.orderId = orderId;}

    public List<String> getProductNames(){
        return productNames;
    }

    public void setProductNames(List<String> products){this.productNames = products;}

    public BigDecimal getTotalPrice(){
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice){this.totalPrice = totalPrice;}

    public Date getDate(){
        return date;
    }

    public void setDate(Date date){this.date=date;}

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }


}
