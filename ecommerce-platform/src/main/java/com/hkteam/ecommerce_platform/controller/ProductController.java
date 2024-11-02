package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.ElasticSearchService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Product Controller")
public class ProductController {
    ProductService productService;

    @PostMapping()
    ApiResponse<ProductCreationResponse> createProduct(@RequestBody @Valid ProductCreationRequest request) {
        return ApiResponse.<ProductCreationResponse>builder()
                .result(productService.createProduct(request))
                .build();
    }

    @PatchMapping("/{id}")
    ApiResponse<ProductDetailResponse> updateProduct(
            @PathVariable String id, @RequestBody @Valid ProductUpdateRequest request) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.updateProduct(id, request))
                .build();
    }

    @GetMapping("/slug/{slug}")
    ApiResponse<ProductDetailResponse> getProductBySlug(@PathVariable String slug) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.getProductBySlug(slug))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ProductDetailResponse> getProduct(@PathVariable String id) {
        return ApiResponse.<ProductDetailResponse>builder()
                .result(productService.getProduct(id))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ApiResponse.<String>builder()
                .result("Product deleted successfully")
                .build();
    }

    @GetMapping("/")
    ApiResponse<PaginationResponse<ProductResponse>> getAllProducts(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "brand", required = false) Long brandId,
            @RequestParam(value = "store", required = false) String storeId,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "minPrice", required = false) BigDecimal  minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "rating", required = false, defaultValue = "0") int minRate,
            @RequestParam(value = "isAvailable", required = false, defaultValue = "1") Boolean isAvailable,
            @RequestParam(value = "isBlocked", required = false, defaultValue = "0") Boolean isBlocked
            )
    {
        return ApiResponse.<PaginationResponse<ProductResponse>>builder()
                .result(productService.getAllProducts(categoryId, brandId, storeId, sortBy, order, page, limit, search, minPrice, maxPrice, minRate, isAvailable, isBlocked))
                .build();
    }

}
