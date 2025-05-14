package com.phatbee.cosmeticshopbackend.Entity;

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
public class Voucher implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voucherId;
    private String voucherCode;
    private Double voucherValue;
//    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean used = false;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    private Order order;



}
