package com.phatbee.cosmeticshopbackend.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long productId;
    private String productName;
    private String image;
    private double price;
}
