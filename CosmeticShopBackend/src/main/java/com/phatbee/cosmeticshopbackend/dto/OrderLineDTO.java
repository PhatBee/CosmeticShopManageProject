package com.phatbee.cosmeticshopbackend.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class OrderLineDTO implements Serializable {
    private Long orderLineId;
    private Long productId; // Explicitly include productId
    private Map<String, Object> productSnapshot;
    private Long quantity;

    // Constructor to map from OrderLine entity
    public OrderLineDTO(com.phatbee.cosmeticshopbackend.Entity.OrderLine orderLine) {
        this.orderLineId = orderLine.getOrderLineId();
        this.productId = orderLine.getProduct() != null ? orderLine.getProduct().getProductId() : null;
        this.productSnapshot = orderLine.getProductSnapshot();
        this.quantity = orderLine.getQuantity();
    }
}