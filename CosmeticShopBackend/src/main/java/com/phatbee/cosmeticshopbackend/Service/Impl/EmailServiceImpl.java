package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendOtp(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Account Activation OTP");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("22110394@student.hcmute.edu.vn");
            helper.setTo(toEmail);
            helper.setSubject("Your OTP Verification Code");

            String content = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                    + "<h2 style='color: #333;'>Welcome to Cosmetics Store!</h2>"
                    + "<p>Please use the following OTP code to complete your registration:</p>"
                    + "<h1 style='color: #007bff; letter-spacing: 2px;'>" + otp + "</h1>"
                    + "<p>This code will expire in 10 minutes.</p>"
                    + "<p>If you did not request this code, please ignore this email.</p>"
                    + "<p>Best regards,<br>Cosmetics Store Team</p>"
                    + "</div>";

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("22110394@student.hcmute.edu.vn");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Verification Code");

            String content = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                    + "<h2 style='color: #333;'>Cosmetics Store Password Reset</h2>"
                    + "<p>You've requested to reset your password. Please use the following code:</p>"
                    + "<h1 style='color: #e91e63; letter-spacing: 2px;'>" + otp + "</h1>"
                    + "<p>This code will expire in 10 minutes.</p>"
                    + "<p>If you did not request this code, please ignore this email and make sure you can still log in to your account.</p>"
                    + "<p>Best regards,<br>Cosmetics Store Team</p>"
                    + "</div>";

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
