package com.phatbee.cosmeticshopbackend.Service;

public interface EmailService {
    public void sendOtp(String to, String otp);
    public void sendOtpEmail(String toEmail, String otp);
    public void sendPasswordResetOtp(String toEmail, String otp);

}
