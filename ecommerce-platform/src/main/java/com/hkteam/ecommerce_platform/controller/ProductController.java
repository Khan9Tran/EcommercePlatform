package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @CacheEvict(value = "productsCache", allEntries = true)
    @PatchMapping("/{id}")
    public ApiResponse<ProductUserViewResponse> updateProduct(
            @PathVariable String id, @RequestBody @Valid ProductUpdateRequest request) {
        return ApiResponse.<ProductUserViewResponse>builder()
                .result(productService.updateProduct(id, request))
                .build();
    }

    @Cacheable(value = "productCache", key = "#slug", unless = "#result == null")
    @GetMapping("/slug/{slug}")
    public ApiResponse<ProductUserViewResponse> getProductBySlug(@PathVariable String slug) {
        log.info("Get product by slug: {}", slug);
        return ApiResponse.<ProductUserViewResponse>builder()
                .result(productService.getProductBySlug(slug))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ProductUserViewResponse> getProduct(@PathVariable String id) {
        return ApiResponse.<ProductUserViewResponse>builder()
                .result(productService.getProduct(id))
                .build();
    }

    @CacheEvict(value = "productsCache", allEntries = true)
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable String id) {
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

    @CacheEvict(value = "productsCache", allEntries = true)
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateProductStatus(@PathVariable String id) {
        return ApiResponse.<Void>builder()
                .result(productService.updateProductStatus(id))
                .build();
    }

    @GetMapping("/top3/store/{storeId}")
    ApiResponse<List<MiniProductResponse>> getProductNewest(@PathVariable String storeId) {
        return ApiResponse.<List<MiniProductResponse>>builder()
                .result(productService.getProductNewest(storeId))
                .build();
    }

    @GetMapping("/best_selling")
    ApiResponse<PaginationResponse<ProductBestSellingResponse>> getProductBestSelling(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "limit", required = false, defaultValue = "10") String limit) {
        return ApiResponse.<PaginationResponse<ProductBestSellingResponse>>builder()
                .result(productService.getProductBestSelling(page, size, limit))
                .build();
    }

    @GetMapping("/best_interaction")
    ApiResponse<PaginationResponse<ProductBestInteractionResponse>> getProductBestInteraction(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "limit", required = false, defaultValue = "10") String limit) {
        return ApiResponse.<PaginationResponse<ProductBestInteractionResponse>>builder()
                .result(productService.getProductBestInteraction(page, size, limit))
                .build();
    }
}
