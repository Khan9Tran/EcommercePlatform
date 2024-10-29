package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.UpdateValueRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ValueDetailResponse;
import com.hkteam.ecommerce_platform.service.ValueService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/values")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Values Controller")
public class ValueController {
    ValueService valueService;

    @PutMapping("/{id}")
    public ApiResponse<ValueDetailResponse> updateValue(
            @PathVariable Long id, @RequestBody @Valid UpdateValueRequest request) {
        log.info("Updating value with id: {}", id);
        return ApiResponse.<ValueDetailResponse>builder()
                .result(valueService.updateValue(id, request))
                .build();
    }
}
