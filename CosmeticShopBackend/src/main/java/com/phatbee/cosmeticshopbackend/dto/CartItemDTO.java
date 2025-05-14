package com.phatbee.cosmeticshopbackend.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private ProductDTO product;
    private Long quantity;
}
