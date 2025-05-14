package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OrderRequest implements Serializable {
    private Long userId;
    private Double total;
    private String orderStatus;
    private LocalDateTime deliveryDate;
    private String paymentMethod;

    // Constructors, getters, and setters
    public OrderRequest() {}
    public OrderRequest(Long userId, Double total, String orderStatus, LocalDateTime deliveryDate, String paymentMethod) {
        this.userId = userId;
        this.total = total;
        this.orderStatus = orderStatus;
        this.deliveryDate = deliveryDate;
        this.paymentMethod = paymentMethod;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}