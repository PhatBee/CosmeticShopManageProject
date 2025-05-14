package com.phatbee.cosmeticshopbackend.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    private String fullName;
    private String phone;
    private String gender;
    private String image;
    private String username;
    private String email;
    private String password;
    private boolean activated = false;

    private String otp; //One-time-password
    private int failedAttempts = 0;
    private LocalDateTime otpGeneratedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "order-user")
    private List<Order> orders;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "address-customer")
    private Set<Address> addresses;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonManagedReference(value = "cart_customer")
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonBackReference(value = "user-wishlist")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Wishlist> wishlists;
}
