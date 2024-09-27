package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.BrandCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.BrandUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.BrandResponse;
import com.hkteam.ecommerce_platform.service.BrandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Brand Controller")
public class BrandController {
    BrandService brandService;

    @Operation(summary = "Create brand", description = "Api create brand")
    @PostMapping()
    public ApiResponse<BrandResponse> createBrand(@RequestBody @Valid BrandCreationRequest request) {
        BrandResponse brandResponse = brandService.createBrand(request);

        return ApiResponse.<BrandResponse>builder().result(brandResponse).build();
    }

    @Operation(summary = "Update brand", description = "Api update brand")
    @PutMapping("/{id}")
    public ApiResponse<BrandResponse> updateBrand(
            @PathVariable Long id, @RequestBody @Valid BrandUpdateRequest request) {
        BrandResponse brandResponse = brandService.updateBrand(id, request);

        return ApiResponse.<BrandResponse>builder().result(brandResponse).build();
    }

    @Operation(summary = "Delete brand", description = "Api delete brand by id")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Get all brands", description = "Api get all brands")
    @GetMapping()
    public ApiResponse<List<BrandResponse>> getAllBrands() {
        return ApiResponse.<List<BrandResponse>>builder()
                .result(brandService.getAllBrands())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one brand by id", description = "Api get one brand by id")
    public ApiResponse<BrandResponse> getOneBrandtById(@PathVariable Long id) {
        BrandResponse brandResponse = brandService.getOneBrandById(id);

        return ApiResponse.<BrandResponse>builder().result(brandResponse).build();
    }
}
