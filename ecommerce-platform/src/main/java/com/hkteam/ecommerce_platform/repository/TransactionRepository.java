package com.hkteam.ecommerce_platform.repository;


import com.hkteam.ecommerce_platform.entity.payment.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
}