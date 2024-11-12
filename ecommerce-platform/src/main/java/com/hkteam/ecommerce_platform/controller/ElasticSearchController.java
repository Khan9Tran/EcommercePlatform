package com.hkteam.ecommerce_platform.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductResponse;
import com.hkteam.ecommerce_platform.service.ElasticSearchService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Search Controller")
public class ElasticSearchController {
    ElasticSearchService elasticSearchService;

    @GetMapping("/autoSuggest")
    ApiResponse<List<String>> getAutoSuggestProduct(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String text) throws IOException {
        return ApiResponse.<List<String>>builder()
                .result(elasticSearchService.autoSuggestionProduct(text))
                .build();
    }

    @GetMapping()
    ApiResponse<PaginationResponse<ProductResponse>> getAutoSuggestProduct(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "brand", required = false) Long brandId,
            @RequestParam(value = "store", required = false) String storeId,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "limit", required = false, defaultValue = "10") String limit,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "rating", required = false, defaultValue = "0") int minRate) {
        return ApiResponse.<PaginationResponse<ProductResponse>>builder()
                .result(elasticSearchService.getAllProducts(
                        categoryId, brandId, storeId, sortBy, order, page, limit, search, minPrice, maxPrice, minRate))
                .build();
    }
}
