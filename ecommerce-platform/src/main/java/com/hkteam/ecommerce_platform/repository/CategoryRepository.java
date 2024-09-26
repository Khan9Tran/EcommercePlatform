package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    Optional<Category> findBySlug(String slug);
}
