package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.status.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, String> {
}