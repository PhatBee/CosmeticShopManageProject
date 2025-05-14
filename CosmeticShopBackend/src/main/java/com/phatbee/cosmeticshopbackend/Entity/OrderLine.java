package com.phatbee.cosmeticshopbackend.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.phatbee.cosmeticshopbackend.Utils.JsonToMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_line")
public class OrderLine implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderLineId;


    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    @JsonBackReference(value = "product-orderline")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;


    @Convert(converter = JsonToMapConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Object> productSnapshot;  // Metadata stored as JSON map


    private Long quantity;

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    @JsonBackReference(value = "order-orderline")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;
}
