package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import com.hkteam.ecommerce_platform.dto.request.StatisticRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
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
    public ApiResponse<ReviewCreationResponse> createReview(@RequestBody @Valid ReviewCreationRequest request) {
        return ApiResponse.<ReviewCreationResponse>builder()
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

    @Operation(summary = "Get all review by id store", description = "Api get all review by id store")
    @GetMapping("/store/{storeId}")
    public ApiResponse<List<ReviewStoreResponse>> getAllReviewByStoreId(@PathVariable String storeId) {
        return ApiResponse.<List<ReviewStoreResponse>>builder()
                .result(reviewService.getAllReviewByStoreId(storeId))
                .build();
    }

    @Operation(summary = "Check all product reviewed", description = "Api check all product reviewed")
    @GetMapping("/order/{orderId}/is_all_order_reviewed")
    public ApiResponse<Boolean> isAllOrderReviewed(@PathVariable String orderId) {
        return ApiResponse.<Boolean>builder()
                .result(reviewService.isAllOrderReviewed(orderId))
                .build();
    }

    @Operation(summary = "Check any product reviewed", description = "Api check any product reviewed")
    @GetMapping("/order/{orderId}/is_any_order_reviewed")
    public ApiResponse<Boolean> isAnyOrderReviewed(@PathVariable String orderId) {
        return ApiResponse.<Boolean>builder()
                .result(reviewService.isAnyOrderReviewed(orderId))
                .build();
    }

    @Operation(summary = "Get all product review by id order", description = "Api get all review by id order")
    @GetMapping("/order/{orderId}/product")
    public ApiResponse<List<ReviewOrderItemResponse>> getAllProductReview(@PathVariable String orderId) {
        return ApiResponse.<List<ReviewOrderItemResponse>>builder()
                .result(reviewService.getAllProductReview(orderId))
                .build();
    }


    @GetMapping("/statistics")
    public ApiResponse<StatisticResponse> getStatistics(@ModelAttribute StatisticRequest request) {
        return ApiResponse.<StatisticResponse>builder()
                .result(reviewService.getStatistics(request))
                .build();
    }

}
