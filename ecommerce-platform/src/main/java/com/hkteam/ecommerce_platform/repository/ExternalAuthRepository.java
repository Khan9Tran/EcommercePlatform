package com.hkteam.ecommerce_platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hkteam.ecommerce_platform.entity.user.ExternalAuth;
import com.hkteam.ecommerce_platform.enums.Provider;

public interface ExternalAuthRepository extends JpaRepository<ExternalAuth, Long> {
    Optional<ExternalAuth> findByProviderAndProviderID(Provider provider, String providerID);
}
