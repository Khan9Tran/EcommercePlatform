package com.hkteam.ecommerce_platform.service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ProductCreationResponse;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;
import com.hkteam.ecommerce_platform.enums.TypeSlug;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ComponentMapper;
import com.hkteam.ecommerce_platform.mapper.ProductMapper;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.SlugUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {
    private final ComponentMapper componentMapper;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductRepository productRepository;
    ComponentRepository componentRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    ProductComponentValueRepository productComponentValueRepository;

    @PreAuthorize("hasRole('SELLER')")
    public ProductCreationResponse createProduct(ProductCreationRequest request) {

        var product = productMapper.toProduct(request);

        product.setSlug(SlugUtils.getSlug(product.getName(), TypeSlug.PRODUCT));

        var owner = authenticatedUserUtil.getAuthenticatedUser();

        if (Objects.isNull(owner.getStore())) throw new AppException(ErrorCode.STORE_NOT_FOUND);

        product.setStore(owner.getStore());

        var category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        product.setCategory(category);

        var brand = brandRepository
                .findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        product.setBrand(brand);

        var componentRequest = request.getComponents();

        Set<Component> components = new HashSet<>();
        product.setProductComponentValues(new HashSet<>());

        componentRequest.forEach((component) -> {
            var cp = componentRepository
                    .findById(component.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));
            product.getProductComponentValues()
                    .add(ProductComponentValue.builder()
                            .value(component.getValue())
                            .component(cp)
                            .product(product)
                            .build());
            components.add(cp);
        });

        if (!Objects.isNull(category.getComponents()) && components.isEmpty())
            throw new AppException(ErrorCode.COMPONENT_NOT_FOUND);

        if (!category.getComponents().equals(components)) throw new AppException(ErrorCode.COMPONENT_NOT_FOUND);

        try {
            productRepository.save(product);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return productMapper.toProductCreationResponse(product);
    }
}
