package ip.project.backend.backend.model;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Document(collection = "order")
public class Order {

    @Id
    private ObjectId id;
    private String orderId;
    private List<String> productNames;
    private BigDecimal totalPrice;
    private Date date;
    private Integer employeeId;

    public Order(){}

    public Order(String orderId, List<String> productNames, BigDecimal totalPrice, Date date, Integer employeeId){
        this.orderId = orderId;
        this.productNames = productNames;
        this.totalPrice = totalPrice;
        this.date = date;
        this.employeeId = employeeId;
    }

    public ObjectId getId(){
        return id;
    }

    public void setId(ObjectId id){this.id = id;}

    public String getOrderId(){
        return orderId;
    }

    public void setOrderId(String orderId){this.orderId = orderId;}

    public List<String> getProducts(){
        return productNames;
    }

    public void setProducts(List<String> products){this.productNames = products;}

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
