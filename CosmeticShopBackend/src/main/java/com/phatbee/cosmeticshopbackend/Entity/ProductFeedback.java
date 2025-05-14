package com.phatbee.cosmeticshopbackend.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "product_feedback")

public class ProductFeedback implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productFeedbackId;
    @Column(columnDefinition = "text")
    private String comment;
    private String image;
    private LocalDateTime feedbackDate;
    // đây là thuộc tính của ProductFeedback, không phải của Customer
    private Long customerId;
    // đây là thuộc tính của ProductFeedback, không phải của Order
    private Long orderId;

    private String productSnapshotName;

    private Double rating;

    //    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;
}
