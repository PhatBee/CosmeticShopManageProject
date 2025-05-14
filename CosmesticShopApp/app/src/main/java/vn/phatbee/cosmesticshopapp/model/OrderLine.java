package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;
import java.util.Map;

public class OrderLine implements Serializable {
    private Long productId;
    private Long quantity;
    private Map<String, Object> productSnapshot;

    // Constructors, getters, and setters
    public OrderLine() {}
    public OrderLine(Long productId, Long quantity, Map<String, Object> productSnapshot) {
        this.productId = productId;
        this.quantity = quantity;
        this.productSnapshot = productSnapshot;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public Map<String, Object> getProductSnapshot() { return productSnapshot; }
    public void setProductSnapshot(Map<String, Object> productSnapshot) { this.productSnapshot = productSnapshot; }
}