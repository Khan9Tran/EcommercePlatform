package com.hkteam.ecommerce_platform.repository;

import java.util.Collection;
import java.util.Optional;

import com.hkteam.ecommerce_platform.enums.RoleName;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hkteam.ecommerce_platform.entity.authorization.Role;
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

    @Query("""
        select u from User u inner join u.roles roles
        where roles.name in ?1 and u.isBlocked in ?2 
        and (lower(u.username) like lower(concat('%', ?3, '%')) 
        or lower(u.name) like lower(concat('%', ?4, '%')))
        """)
    Page<User> searchByBlocked(Collection<RoleName> names, Collection<Boolean> isBlockeds, String username, String name, Pageable pageable);

    @Query("""
        select u from User u inner join u.roles roles
        where roles.name in ?1
        and (lower(u.username) like lower(concat('%', ?2, '%')) 
        or lower(u.name) like lower(concat('%', ?3, '%')))
        """)
    Page<User> search(Collection<RoleName> names, String username, String name, Pageable pageable);

}
