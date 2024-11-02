package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.service.ElasticSearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Search Controller")
public class ElasticSearchController {
    ElasticSearchService elasticSearchService;
    @GetMapping("/autoSuggest")
    ApiResponse<List<String>> getAutoSuggestProduct(@RequestParam(value = "keyword", required = false, defaultValue = "") String text) throws IOException {
        return  ApiResponse.<List<String>>builder().result(elasticSearchService.autoSuggestionProduct(text)).build();
    }
}
