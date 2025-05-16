package com.phatbee.cosmeticshopbackend.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String fullName;
    private String phone;
    private String gender;
    private String image;

}