package com.phatbee.cosmeticshopbackend.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "address")

public class Address implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    @Column(columnDefinition = "text")
    private String receiverName;
    private String receiverPhone;
    @Column(columnDefinition = "text")
    private String address;
    @Column(columnDefinition = "text")
    private String province;
    @Column(columnDefinition = "text")
    private String district;
    @Column(columnDefinition = "text")
    private String ward;
    @Column(name = "is_default")
    private boolean defaultAddress;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JsonBackReference(value = "address-customer")
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User customer;
}
