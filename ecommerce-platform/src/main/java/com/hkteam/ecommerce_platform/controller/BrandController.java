package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.BrandCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.BrandUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.BrandResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
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
        return ApiResponse.<Void>builder().message("Deleted brand successfully").build();
    }

    @Operation(summary = "Get all brands", description = "Api get all brands")
    @GetMapping()
    ApiResponse<PaginationResponse<BrandResponse>> getAllBrands(
            @RequestParam(value = "sort", required = false, defaultValue = "") String sort,
            @RequestParam(value = "tab", required = false, defaultValue = "all") String tab,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        return ApiResponse.<PaginationResponse<BrandResponse>>builder()
                .result(brandService.getAllBrands(page, size, tab, sort, search))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one brand by id", description = "Api get one brand by id")
    public ApiResponse<BrandResponse> getOneBrandById(@PathVariable Long id) {
        BrandResponse brandResponse = brandService.getOneBrandById(id);

        return ApiResponse.<BrandResponse>builder().result(brandResponse).build();
    }
}
