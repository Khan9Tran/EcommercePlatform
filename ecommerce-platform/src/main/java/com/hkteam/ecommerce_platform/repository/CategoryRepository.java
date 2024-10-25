package com.hkteam.ecommerce_platform.repository;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findBySlug(String slug);

    @NotNull
    Page<Category> findAll(@NotNull Pageable pageable);

    @NotNull
    List<Category> findAllById(@NotNull Iterable<Long> ids);

    @Query("""
			select c from Category c
			where lower(c.name) like lower(concat('%', ?1, '%'))""")
    Page<Category> searchAllCategory(String name, Pageable pageable);
}
