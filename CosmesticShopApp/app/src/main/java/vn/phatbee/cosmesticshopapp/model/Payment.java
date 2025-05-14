package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;

public class Payment implements Serializable {
    private String paymentMethod;
    private String paymentStatus;
    private Double total;
    private String paymentDate;

    // Constructors, getters, and setters
    public Payment() {}
    public Payment(String paymentMethod, String paymentStatus, Double total, String paymentDate) {
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.total = total;
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
}