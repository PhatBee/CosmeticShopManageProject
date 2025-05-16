package com.phatbee.cosmeticshopbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.phatbee.cosmeticshopbackend.Entity.Payment;
import com.phatbee.cosmeticshopbackend.Entity.ShippingAddress;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderDTO implements Serializable {
    private int orderId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;
    private Double total;
    private String orderStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryDate;
    private String transactionId;
    private List<OrderLineDTO> orderLines;
    private Payment payment;
    private ShippingAddress shippingAddress;

    // Constructor to map from Order entity
    public OrderDTO(com.phatbee.cosmeticshopbackend.Entity.Order order) {
        this.orderId = order.getOrderId();
        this.orderDate = order.getOrderDate();
        this.total = order.getTotal();
        this.orderStatus = order.getOrderStatus();
        this.deliveryDate = order.getDeliveryDate();
        this.transactionId = order.getTransactionId();
        this.orderLines = order.getOrderLines().stream()
                .map(OrderLineDTO::new)
                .collect(Collectors.toList());
        this.payment = order.getPayment();
        this.shippingAddress = order.getShippingAddress();
    }
}