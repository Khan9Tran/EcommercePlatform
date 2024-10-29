package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.UpdateVariantRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.VariantDetailResponse;
import com.hkteam.ecommerce_platform.service.VariantService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Variants Controller")
public class VariantController {
    VariantService variantService;

    @PatchMapping("/{id}")
    public ApiResponse<VariantDetailResponse> updateVariant(
            @PathVariable String id, @RequestBody @Valid UpdateVariantRequest request) {
        log.info("Updating variant with id: {}", id);
        return ApiResponse.<VariantDetailResponse>builder()
                .result(variantService.updateVariant(id, request))
                .build();
    }
}
