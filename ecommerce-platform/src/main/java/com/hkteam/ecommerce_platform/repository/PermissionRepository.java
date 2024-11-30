package com.hkteam.ecommerce_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkteam.ecommerce_platform.entity.authorization.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {}
