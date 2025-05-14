package com.phatbee.cosmeticshopbackend.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class CartItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;


    @ManyToOne
//    @JsonBackReference(value = "product-cartitem")
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    private Long quantity;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JsonBackReference(value = "cart-cartitem")
    @JoinColumn(name = "cart_id", referencedColumnName = "cart_id")
    private Cart cart;
}
