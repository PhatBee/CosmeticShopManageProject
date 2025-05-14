package com.phatbee.cosmeticshopbackend.Repository;

import com.phatbee.cosmeticshopbackend.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
