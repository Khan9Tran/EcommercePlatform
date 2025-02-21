package com.hkteam.ecommerce_platform.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.dto.request.ViewProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ViewProductCreationResponse;
import com.hkteam.ecommerce_platform.entity.embed.ViewProductKey;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.entity.useractions.ViewProduct;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.repository.ViewProductRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ViewProductService {
    ViewProductRepository viewProductRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    ProductRepository productRepository;

    @PreAuthorize("hasRole('USER')")
    public ViewProductCreationResponse createViewProduct(ViewProductCreationRequest request) {
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        User user = authenticatedUserUtil.getAuthenticatedUser();

        ViewProductKey viewProductKey = new ViewProductKey(user.getId(), product.getId());

        if (viewProductRepository.findById(viewProductKey).isPresent()) {
            throw new AppException(ErrorCode.VIEW_PRODUCT_ALREADY_EXIST);
        }

        ViewProduct viewProduct = new ViewProduct();
        viewProduct.setId(viewProductKey);
        viewProduct.setCount(0);
        viewProduct.setUser(user);
        viewProduct.setProduct(product);

        try {
            ViewProduct savedViewProduct = viewProductRepository.save(viewProduct);

            ViewProductCreationResponse response = new ViewProductCreationResponse();
            response.setUserId(savedViewProduct.getId().getUserId());
            response.setProductId(savedViewProduct.getId().getProductId());
            response.setCount(savedViewProduct.getCount());

            log.info("Saved View Product: {}", savedViewProduct.getId().getProductId());

            return response;
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void changeCountViewProduct(String productId) {
        String userId = authenticatedUserUtil.getAuthenticatedUser().getId();

        ViewProductKey viewProductKey = new ViewProductKey(userId, productId);

        if (viewProductRepository.findById(viewProductKey).isEmpty()) {
            log.info("View Product Not Found: {}", productId);
            ViewProductCreationRequest request = new ViewProductCreationRequest();
            request.setProductId(productId);
            createViewProduct(request);
        }

        ViewProduct viewProduct = viewProductRepository
                .findById(viewProductKey)
                .orElseThrow(() -> new AppException(ErrorCode.VIEW_PRODUCT_NOT_FOUND));

        viewProduct.setCount(viewProduct.getCount() + 1);

        try {
            viewProductRepository.save(viewProduct);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }
}
