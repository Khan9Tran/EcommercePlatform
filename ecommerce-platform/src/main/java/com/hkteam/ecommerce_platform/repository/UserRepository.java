package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;
import java.util.Set;

import com.hkteam.ecommerce_platform.entity.authorization.Role;
import com.hkteam.ecommerce_platform.enums.RoleName;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    @NotNull
    Optional<User> findById(@NotNull String id);

    Optional<User> findByEmailValidationToken(String token);

    Optional<User> findByEmail(String email);

    Page<User> findByRoles(Role role, @NotNull Pageable pageable);
    Page<User> findByRolesAndIsBlocked(Role role, boolean isBlocked, @NotNull Pageable pageable);
}
