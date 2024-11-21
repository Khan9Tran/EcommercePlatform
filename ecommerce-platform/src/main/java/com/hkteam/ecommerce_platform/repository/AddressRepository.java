package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.user.Address;
import com.hkteam.ecommerce_platform.entity.user.User;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    @NotNull
    Optional<Address> findById(@NotNull Long id);

    @NotNull
    Page<Address> findAll(@NotNull Pageable pageable);

    @NotNull
    Optional<Address> findByIdAndUserId(@NotNull Long id, @NotNull String userId);

    @NotNull
    Page<Address> findAllByUser(@NotNull User user, @NotNull Pageable pageable);
}
