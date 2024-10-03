package com.hkteam.ecommerce_platform.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ImageResponse;
import com.hkteam.ecommerce_platform.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageController {
    ImageService imageService;

    @Operation(summary = "Upload Category Image", description = "Upload an image for a category")
    @PostMapping(value = "/categories/{id}", consumes = "multipart/form-data")
    public ApiResponse<ImageResponse> uploadCategoryImage(
            @RequestParam("image") MultipartFile image, @PathVariable("id") Long categoryId) {
        return ApiResponse.<ImageResponse>builder()
                .result(imageService.uploadCategoryImage(image, categoryId))
                .build();
    }

    @Operation(summary = "Delete Category Image", description = "Delete an image for a category")
    @DeleteMapping(value = "/categories/{id}")
    public ApiResponse<Void> deleteCategoryImage(@PathVariable("id") Long categoryId) {
        imageService.deleteCategoryImage(categoryId);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Upload user image", description = "Upload an image for a user")
    @PostMapping(value = "/users", consumes = "multipart/form-data")
    public ApiResponse<ImageResponse> uploadUserImage(@RequestParam("image") MultipartFile image) {
        return ApiResponse.<ImageResponse>builder()
                .result(imageService.uploadUserImage(image))
                .build();
    }

    @Operation(summary = "Delete User Image", description = "Delete an image for a category")
    @DeleteMapping(value = "/users")
    public ApiResponse<Void> deleteUserImage() {
        imageService.deleteUserImage();
        return ApiResponse.<Void>builder().build();
    }
}
