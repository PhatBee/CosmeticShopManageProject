package com.phatbee.cosmeticshopbackend.dto;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String gender;
    private String username;
    private String email;
    private String password;
}
