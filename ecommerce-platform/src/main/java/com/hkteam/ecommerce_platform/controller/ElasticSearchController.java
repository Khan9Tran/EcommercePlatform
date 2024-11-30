package com.hkteam.ecommerce_platform.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "autoSuggestCache", key = "#text", unless = "#result == null || #result.result.isEmpty()")
    @GetMapping("/auto-suggest")
    public ApiResponse<List<String>> getAutoSuggestProduct(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String text) throws IOException {
        log.info("Auto suggest product with keyword: {}", text);
        return ApiResponse.<List<String>>builder()
                .result(elasticSearchService.autoSuggestionProduct(text))
                .build();
    }

    @Cacheable(
            value = "searchCache",
            key =
                    "{#categoryIds, #brandIds, #storeId, #sortBy, #order, #page, #limit, #search, #minPrice, #maxPrice, #minRate}" // Key cache
            )
    @GetMapping()
    public ApiResponse<PaginationResponse<ProductResponse>> getAutoSuggestProduct(
            @RequestParam(value = "categories", required = false) List<Long> categoryIds,
            @RequestParam(value = "brands", required = false) List<Long> brandIds,
            @RequestParam(value = "store", required = false) String storeId,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "limit", required = false, defaultValue = "10") String limit,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "rating", required = false, defaultValue = "0") int minRate) {
        log.info(
                "Search product with categories: {}, brands: {}, store: {}, sortBy: {}, order: {}, page: {}, limit: {}, search: {}, minPrice: {}, maxPrice: {}, minRate: {}",
                categoryIds,
                brandIds,
                storeId,
                sortBy,
                order,
                page,
                limit,
                search,
                minPrice,
                maxPrice,
                minRate);
        return ApiResponse.<PaginationResponse<ProductResponse>>builder()
                .result(elasticSearchService.getAllProducts(
                        categoryIds,
                        brandIds,
                        storeId,
                        sortBy,
                        order,
                        page,
                        limit,
                        search,
                        minPrice,
                        maxPrice,
                        minRate))
                .build();
    }
}
