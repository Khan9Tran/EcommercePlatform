package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.product.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
}
