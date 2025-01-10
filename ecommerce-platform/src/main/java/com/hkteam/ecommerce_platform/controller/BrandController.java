package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.BrandCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.BrandUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.BrandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Brand Controller")
public class BrandController {
    BrandService brandService;

    @Operation(summary = "Create brand", description = "Api create brand")
    @PostMapping()
    public ApiResponse<BrandCreationResponse> createBrand(@RequestBody @Valid BrandCreationRequest request) {
        return ApiResponse.<BrandCreationResponse>builder()
                .result(brandService.createBrand(request))
                .build();
    }

    @Operation(summary = "Update brand", description = "Api update brand by id")
    @PutMapping("/{brandId}")
    public ApiResponse<BrandUpdateResponse> updateBrand(
            @PathVariable Long brandId, @RequestBody @Valid BrandUpdateRequest request) {
        return ApiResponse.<BrandUpdateResponse>builder()
                .result(brandService.updateBrand(brandId, request))
                .build();
    }

    @Operation(summary = "Delete brand", description = "Api delete brand by id")
    @DeleteMapping("/{brandId}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long brandId) {
        brandService.deleteBrand(brandId);
        return ApiResponse.<Void>builder().message("Brand deleted successfully").build();
    }

    @Operation(summary = "Get all brand", description = "Api get all brand")
    @GetMapping()
    ApiResponse<PaginationResponse<BrandResponse>> getAllBrand(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "orderBy", required = false, defaultValue = "") String orderBy,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        return ApiResponse.<PaginationResponse<BrandResponse>>builder()
                .result(brandService.getAllBrand(page, size, sortBy, orderBy, search))
                .build();
    }

    @Operation(summary = "Get all brands", description = "Api get all brands")
    @GetMapping("/all")
    ApiResponse<List<BrandResponse>> getAllBrands(
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        return ApiResponse.<List<BrandResponse>>builder()
                .result(brandService.getAllBrands(search))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one brand by id", description = "Api get one brand by id")
    public ApiResponse<BrandResponse> getOneBrandById(@PathVariable Long id) {
        return ApiResponse.<BrandResponse>builder()
                .result(brandService.getOneBrandById(id))
                .build();
    }
}
