package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ReviewUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Review Controller")
public class ReviewController {
    ReviewService reviewService;

    @Operation(summary = "Create review", description = "Api create review")
    @PostMapping()
    public ApiResponse<ReviewResponse> createReview(@RequestBody @Valid ReviewCreationRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.createReview(request))
                .build();
    }

    @Operation(summary = "Get review one product", description = "Api get review one product")
    @GetMapping("/product/{productId}")
    public ApiResponse<PaginationResponse<ReviewOneProductResponse>> getReviewOneProduct(
            @PathVariable String productId,
            @RequestParam(value = "starNumber", required = false, defaultValue = "") String starNumber,
            @RequestParam(value = "commentString", required = false, defaultValue = "") String commentString,
            @RequestParam(value = "mediaString", required = false, defaultValue = "") String mediaString,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "sort", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "order", required = false) String orderBy) {
        return ApiResponse.<PaginationResponse<ReviewOneProductResponse>>builder()
                .result(reviewService.getReviewOneProduct(
                        productId, starNumber, commentString, mediaString, page, size, sortBy, orderBy))
                .build();
    }

    @Operation(summary = "Get comment and media total review", description = "Api get comment and media total review")
    @GetMapping("/product/{productId}/comment-media-total")
    public ApiResponse<ReviewCountResponse> getCommentAndMediaTotalReview(@PathVariable String productId) {
        return ApiResponse.<ReviewCountResponse>builder()
                .result(reviewService.getCommentAndMediaTotalReview(productId))
                .build();
    }

    @Operation(summary = "Update review", description = "Api update review")
    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long reviewId, @RequestBody @Valid ReviewUpdateRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.updateReview(reviewId, request))
                .build();
    }

    @Operation(summary = "Delete review by id", description = "Api delete review by id")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ApiResponse.<Void>builder()
                .message("Deleted review successfully")
                .build();
    }
}
