package com.hkteam.ecommerce_platform.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.service.ImageService;

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

    @PostMapping("/categories/")
    public ApiResponse<Object> uploadCategoryImage(@RequestParam("image") MultipartFile image) {
        return ApiResponse.<Object>builder()
                .result(imageService.uploadCategoryImage(image))
                .build();
    }
}
