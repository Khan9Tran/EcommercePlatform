package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.embed.ViewProductKey;
import com.hkteam.ecommerce_platform.entity.useractions.ViewProduct;

@Repository
public interface ViewProductRepository extends JpaRepository<ViewProduct, ViewProductKey> {}
