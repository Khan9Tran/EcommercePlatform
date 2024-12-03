package com.hkteam.ecommerce_platform.service;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.UpdateVariantRequest;
import com.hkteam.ecommerce_platform.dto.response.VariantDetailResponse;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ProductMapper;
import com.hkteam.ecommerce_platform.mapper.ValuesMapper;
import com.hkteam.ecommerce_platform.mapper.VariantMapper;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.repository.VariantRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VariantService {
    VariantMapper variantMapper;
    ProductMapper productMapper;
    ValuesMapper valueMapper;
    VariantRepository variantRepository;
    ProductRepository productRepository;
    ProductService productService;
    AuthenticatedUserUtil authenticatedUserUtil;

    @PreAuthorize("hasRole('SELLER')")
    public VariantDetailResponse updateVariant(String id, UpdateVariantRequest request) {

        var variant = variantRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (!authenticatedUserUtil.isOwner(variant)) throw new AppException(ErrorCode.UNAUTHORIZED);

        if (request.getSalePrice().compareTo(request.getOriginalPrice()) > 0) {
            throw new AppException(ErrorCode.SALE_CANT_GREATER_THAN_ORIGINAL_PRICE);
        }

        var product = variant.getProduct();
        if (Objects.isNull(product)) throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

        product.setQuantity(product.getQuantity() - variant.getQuantity() + request.getQuantity());

        variantMapper.updateVariantFromRequest(request, variant);

        product.setSalePrice(productService.findMinSalePrice(product.getVariants()));
        product.setOriginalPrice(productService.findMaxOriginalPrice(product.getVariants()));

        try {
            productRepository.save(product);
        } catch (Exception e) {
            log.error("Error while updating variant", e);
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return map(variant);
    }

    private VariantDetailResponse map(Variant variant) {
        var v = variantMapper.toVariantDetailResponse(variant);
        v.setProduct(productMapper.toProductOfVariantResponse(variant.getProduct()));
        v.setValues(variant.getValues().stream()
                .map(valueMapper::toValueOfVariantResponse)
                .collect(Collectors.toList()));

        return v;
    }
}
