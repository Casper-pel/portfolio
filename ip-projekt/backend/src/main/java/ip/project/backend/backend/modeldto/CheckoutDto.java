package ip.project.backend.backend.modeldto;

import java.util.List;

public class CheckoutDto {

    private List<ProductWithQuantity> products;


    public CheckoutDto(List<ProductWithQuantity> products) {
        this.products = products;
    }


    public List<ProductWithQuantity> getProducts() {
        return products;
    }

    public void setProducts(List<ProductWithQuantity> products) {
        this.products = products;
    }


}
