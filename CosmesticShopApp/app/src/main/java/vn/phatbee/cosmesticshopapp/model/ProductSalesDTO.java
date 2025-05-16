package vn.phatbee.cosmesticshopapp.model;

import com.google.gson.annotations.SerializedName;

public class ProductSalesDTO {
    @SerializedName("product")
    private Product product;

    @SerializedName("totalQuantity")
    private Long totalQuantity;

    public ProductSalesDTO(Product product, Long totalQuantity) {
        this.product = product;
        this.totalQuantity = totalQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}