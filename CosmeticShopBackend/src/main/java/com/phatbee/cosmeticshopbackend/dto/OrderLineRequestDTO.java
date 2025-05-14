package com.phatbee.cosmeticshopbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineRequestDTO {
    private Long productId;
    private Long quantity;
    private Map<String, Object> productSnapshot;

}
