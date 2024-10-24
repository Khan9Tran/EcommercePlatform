package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.StoreRegistrationRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Store Controller")
public class StoreController {
    StoreService storeService;

    @Operation(summary = "Get all store", description = "Api get all store")
    @GetMapping()
    ApiResponse<PaginationResponse<StoreResponse>> getAllStores(
            @RequestParam(value = "date", required = false, defaultValue = "") String sortDate,
            @RequestParam(value = "name", required = false, defaultValue = "") String sortName,
            @RequestParam(value = "tab", required = false, defaultValue = "all") String tab,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size) {
        return ApiResponse.<PaginationResponse<StoreResponse>>builder()
                .result(storeService.getAllStores(page, size, tab, sortDate, sortName))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one store by id", description = "Api get one store by id")
    public ApiResponse<StoreDetailResponse> getOneStoreById(@PathVariable String id) {
        StoreDetailResponse storeDetailResponse = storeService.getOneStoreById(id);

        return ApiResponse.<StoreDetailResponse>builder()
                .result(storeDetailResponse)
                .build();
    }

    @Operation(summary = "Register store", description = "Api register store")
    @PostMapping("/register-store")
    ApiResponse<StoreRegistrationResponse> registerStore(@RequestBody @Valid StoreRegistrationRequest request) {
        return ApiResponse.<StoreRegistrationResponse>builder()
                .result(storeService.registerStore(request))
                .build();
    }
}
