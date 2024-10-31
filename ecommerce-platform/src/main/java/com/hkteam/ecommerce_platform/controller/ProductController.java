package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductCreationResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductDetailResponse;
import com.hkteam.ecommerce_platform.service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

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
    ApiResponse<PaginationResponse<ProductDetailResponse>> getAllProducts(
            @RequestParam(value = "category", required = false, defaultValue = "") Long categoryId,
            @RequestParam(value = "brand", required = false, defaultValue = "") Long brandId,
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "order", required = false, defaultValue = "") String order,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "minPrice", required = false, defaultValue = "0") BigDecimal  minPrice,
            @RequestParam(value = "maxPrice", required = false, defaultValue = "0") BigDecimal maxPrice,
            @RequestParam(value = "rating", required = false, defaultValue = "0") int minRate
            )
    {
        return ApiResponse.<PaginationResponse<ProductDetailResponse>>builder()
                .result(productService.getAllProducts(categoryId, brandId, sortBy, order, page, limit, search, minPrice, maxPrice, minRate))
                .build();
    }

}
