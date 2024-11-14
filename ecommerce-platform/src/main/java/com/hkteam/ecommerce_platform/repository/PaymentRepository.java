package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}