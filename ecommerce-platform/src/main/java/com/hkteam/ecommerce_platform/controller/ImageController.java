package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ImageResponse;
import com.hkteam.ecommerce_platform.service.ImageService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

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
}
