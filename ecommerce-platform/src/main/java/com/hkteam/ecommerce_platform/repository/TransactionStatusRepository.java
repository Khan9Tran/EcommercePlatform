package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkteam.ecommerce_platform.entity.status.TransactionStatus;

public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, String> {}
