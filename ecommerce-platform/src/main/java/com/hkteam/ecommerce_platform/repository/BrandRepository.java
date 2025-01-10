package com.hkteam.ecommerce_platform.repository;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.product.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByNameIgnoreCase(String name);

    @Query(
            value =
                    """
				select b from Brand b
				where (:search = '' or lower(b.name) like lower(concat('%', :search, '%')))
			""")
    Page<Brand> findAllBrand(@NotNull String search, @NotNull Pageable pageable);

    List<Brand> findByNameIgnoreCase(@Nullable String name);
}
