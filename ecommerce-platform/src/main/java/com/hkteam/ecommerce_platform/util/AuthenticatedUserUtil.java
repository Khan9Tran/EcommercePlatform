package com.hkteam.ecommerce_platform.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.product.Value;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserUtil {
    private final UserRepository userRepository;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public Boolean isOwner(Value value) {
        return isOwner(
                value.getVariants().stream().findFirst().orElseThrow(() -> new AppException(ErrorCode.UNKNOWN_ERROR)));
    }

    public Boolean isOwner(Variant variant) {
        return isOwner(variant.getProduct());
    }

    public Boolean isOwner(Product product) {
        return isOwner(product.getStore());
    }

    public Boolean isOwner(Store store) {
        return getAuthenticatedUser().getId().equals(store.getUser().getId());
    }
}
