package com.phatbee.cosmeticshopbackend.dto;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String otp;
    private String email;
}
