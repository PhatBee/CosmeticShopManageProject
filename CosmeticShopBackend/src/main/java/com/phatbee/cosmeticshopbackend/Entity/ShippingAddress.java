package com.phatbee.cosmeticshopbackend.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "shipping_address")

public class ShippingAddress implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shippingAddressId;
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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JsonBackReference
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Order order;
}
