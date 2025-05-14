package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Order implements Serializable {
    private int orderId;
    private LocalDateTime orderDate;
    private Double total;
    private String orderStatus;
    private LocalDateTime deliveryDate;
    private List<OrderLine> orderLines;
    private Payment payment;
    private ShippingAddress shippingAddress;

    // Getters and setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    public List<OrderLine> getOrderLines() { return orderLines; }
    public void setOrderLines(List<OrderLine> orderLines) { this.orderLines = orderLines; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }
}