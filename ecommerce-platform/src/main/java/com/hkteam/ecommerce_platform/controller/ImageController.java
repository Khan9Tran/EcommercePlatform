package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.request.DeleteProductImageRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductImageUploadRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ImageResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductImageResponse;
import com.hkteam.ecommerce_platform.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Image Controller")
public class ImageController {
    ImageService imageService;

    @Operation(summary = "Upload Category Image", description = "Api upload category image")
    @PostMapping(value = "/categories/{categoryId}", consumes = "multipart/form-data")
    public ApiResponse<ImageResponse> uploadCategoryImage(
            @RequestParam("image") MultipartFile image, @PathVariable("categoryId") Long categoryId) {
        return ApiResponse.<ImageResponse>builder()
                .result(imageService.uploadCategoryImage(image, categoryId))
                .build();
    }

    @Operation(summary = "Delete Category Image", description = "Api delete category image")
    @DeleteMapping(value = "/categories/{categoryId}")
    public ApiResponse<Void> deleteCategoryImage(@PathVariable("categoryId") Long categoryId) {
        imageService.deleteCategoryImage(categoryId);
        return ApiResponse.<Void>builder()
                .message("Deleted category image successfully")
                .build();
    }

    @Operation(summary = "Upload User Image", description = "Api upload user image")
    @PostMapping(value = "/users", consumes = "multipart/form-data")
    public ApiResponse<ImageResponse> uploadUserImage(@RequestParam("image") MultipartFile image) {
        return ApiResponse.<ImageResponse>builder()
                .result(imageService.uploadUserImage(image))
                .build();
    }

    @Operation(summary = "Delete User Image", description = "Api delete user image")
    @DeleteMapping(value = "/users")
    public ApiResponse<Void> deleteUserImage() {
        imageService.deleteUserImage();
        return ApiResponse.<Void>builder()
                .message("Deleted user image successfully")
                .build();
    }

    @Operation(summary = "Upload Brand Logo", description = "Api upload brand logo")
    @PostMapping(value = "/brands/{brandId}", consumes = "multipart/form-data")
    public ApiResponse<ImageResponse> uploadBrandLogo(
            @RequestParam("logoUrl") MultipartFile logoUrl, @PathVariable("brandId") Long brandId) {
        return ApiResponse.<ImageResponse>builder()
                .result(imageService.uploadBrandLogo(logoUrl, brandId))
                .build();
    }

    @Operation(summary = "Delete Brand Logo", description = "Api delete brand logo")
    @DeleteMapping(value = "/brands/{brandId}")
    public ApiResponse<Void> deleteBrandLogo(@PathVariable("brandId") Long brandId) {
        imageService.deleteBrandLogo(brandId);
        return ApiResponse.<Void>builder()
                .message("Deleted brand logo successfully")
                .build();
    }

    @Operation(summary = "Upload Product Main Image", description = "Api upload product main image")
    @PostMapping(value = "/products/{productId}", consumes = "multipart/form-data")
    public ApiResponse<ImageResponse> uploadProductMainImage(
            @RequestParam("mainImage") MultipartFile mainImage, @PathVariable("productId") Long productId) {
        return ApiResponse.<ImageResponse>builder()
                .result(imageService.uploadProductMainImage(mainImage, productId))
                .build();
    }

    @Operation(summary = "Delete Product Main Image", description = "Api delete product main image")
    @DeleteMapping(value = "/products/{productId}")
    public ApiResponse<Void> deleteProductMainImage(@PathVariable("productId") Long productId) {
        imageService.deleteProductMainImage(productId);
        return ApiResponse.<Void>builder()
                .message("Deleted product main image successfully")
                .build();
    }

    @Operation(summary = "Upload Product List Image", description = "Api upload product list image")
    @PostMapping(value = "/products/{productId}/list", consumes = "multipart/form-data")
    public ApiResponse<ProductImageResponse> uploadProductListImage(
            @ModelAttribute ProductImageUploadRequest request, @PathVariable("productId") Long productId) {
        return ApiResponse.<ProductImageResponse>builder()
                .result(imageService.uploadProductListImage(request, productId))
                .build();
    }

    @Operation(summary = "Delete Product List Image", description = "Api delete product list image")
    @DeleteMapping("/products/{productId}/list")
    public ApiResponse<Void> deleteProductListImage(
            @PathVariable("productId") Long productId, @RequestBody @Valid DeleteProductImageRequest request) {
        imageService.deleteProductListImage(productId, request);
        return ApiResponse.<Void>builder()
                .message("Deleted product list image successfully")
                .build();
    }
}
