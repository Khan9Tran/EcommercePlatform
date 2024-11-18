package com.hkteam.ecommerce_platform.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.VideoResponse;
import com.hkteam.ecommerce_platform.service.VideoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Video Controller")
public class VideoController {
    VideoService videoService;

    @Operation(summary = "Upload Product Video", description = "Api upload product video")
    @PostMapping(value = "/products/{productId}", consumes = "multipart/form-data")
    public ApiResponse<VideoResponse> uploadProductVideo(
            @PathVariable("productId") String productId, @RequestParam("videoFile") MultipartFile videoFile) {
        return ApiResponse.<VideoResponse>builder()
                .result(videoService.uploadVideoProduct(productId, videoFile))
                .build();
    }

    @Operation(summary = "Delete Product Video", description = "Api delete product video")
    @DeleteMapping(value = "/products/{productId}")
    public ApiResponse<Void> deleteProductVideo(@PathVariable("productId") Long productId) {
        videoService.deleteProductVideo(productId);
        return ApiResponse.<Void>builder()
                .message("Deleted product video successfully")
                .build();
    }
}
