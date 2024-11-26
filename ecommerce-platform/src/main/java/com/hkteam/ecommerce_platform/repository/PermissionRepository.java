package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.authorization.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {}
