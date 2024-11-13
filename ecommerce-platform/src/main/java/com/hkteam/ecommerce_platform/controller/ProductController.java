package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.ProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "tab", required = false, defaultValue = "available") String tab,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        return ApiResponse.<PaginationResponse<ProductResponse>>builder()
                .result(productService.getAllProducts(sortBy, order, tab, page, size, search))
                .build();
    }

    @PatchMapping("/{id}/status")
    ApiResponse<Void> updateProductStatus(@PathVariable String id) {
        return ApiResponse.<Void>builder()
                .result(productService.updateProductStatus(id))
                .build();
    }
}
