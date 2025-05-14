package com.phatbee.cosmeticshopbackend.dto;

import lombok.Data;

@Data
public class BannerDTO {
    private String title;
    private String imageUrl;
    private String actionUrl;
    private Integer displayOrder;
    private Boolean active;
}
