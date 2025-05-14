package com.phatbee.cosmeticshopbackend.Config;

import java.util.Random;

public class OTPGenerator {
    public static String generateOTP() {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000); //6-digit OTP
        return String.valueOf(otp);
    }
}
