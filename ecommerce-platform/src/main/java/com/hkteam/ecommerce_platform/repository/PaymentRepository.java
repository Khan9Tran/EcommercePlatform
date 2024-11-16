package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkteam.ecommerce_platform.entity.payment.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {}
