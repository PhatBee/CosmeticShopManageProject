package com.phatbee.cosmeticshopbackend.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cart")
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    // những cartitem không còn trong cart nữa, dưới db vẫn còn tham chiếu, dùng orphanRemoval = true để xoá dưới db
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference(value = "cart-cartitem")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<CartItem> cartItems;


    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonBackReference(value = "cart_customer")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User customer;
}
