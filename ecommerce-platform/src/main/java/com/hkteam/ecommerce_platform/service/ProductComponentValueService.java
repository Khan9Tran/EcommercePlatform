package com.hkteam.ecommerce_platform.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ProductComponentValueUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ProductComponentValueResponse;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ProductComponentValueMapper;
import com.hkteam.ecommerce_platform.repository.ProductComponentValueRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductComponentValueService {
    ProductComponentValueRepository productComponentValueRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    ProductComponentValueMapper productComponentValueMapper;

    @PreAuthorize("hasRole('SELLER')")
    public ProductComponentValueResponse update(Long id, ProductComponentValueUpdateRequest request) {
        var pcv = productComponentValueRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_COMPONENT_VALUE_NOT_FOUND));

        if (!authenticatedUserUtil.isOwner(pcv.getProduct())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (pcv.getComponent().isRequired() && request.getValue().isEmpty()) {
            throw new AppException(ErrorCode.REQUIRED_NOT_EMPTY);
        }

        pcv.setValue(request.getValue());

        try {
            productComponentValueRepository.save(pcv);
        } catch (Exception e) {
            log.error("Error when save cpv {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return productComponentValueMapper.toProductComponentValueResponse(pcv);
    }
}
