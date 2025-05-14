package com.phatbee.cosmeticshopbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShippingAddressRequestDTO {
    private String receiverName;
    private String receiverPhone;
    private String address;
    private String province;
    private String district;
    private String ward;
}
