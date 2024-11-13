package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ProductComponentValueUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductComponentValueResponse;
import com.hkteam.ecommerce_platform.service.ProductComponentValueService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/product-component-values")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Product Component Value Controller")
public class ProductComponentValueController {
    ProductComponentValueService productComponentValueService;

    @PutMapping("/{id}")
    public ApiResponse<ProductComponentValueResponse> update(
            @PathVariable Long id, @RequestBody @Valid ProductComponentValueUpdateRequest request) {
        return ApiResponse.<ProductComponentValueResponse>builder()
                .result(productComponentValueService.update(id, request))
                .build();
    }
}
