package com.phatbee.cosmeticshopbackend.Config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Main {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("123");
        System.out.println(hashedPassword);
    }
}
