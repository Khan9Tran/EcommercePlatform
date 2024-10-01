package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.product.Brand;
import com.hkteam.ecommerce_platform.entity.user.ExternalAuth;
import com.hkteam.ecommerce_platform.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalAuthRepository extends JpaRepository<ExternalAuth, Long> {
}
