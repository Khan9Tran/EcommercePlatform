package com.hkteam.ecommerce_platform.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.StoreRegistrationRequest;
import com.hkteam.ecommerce_platform.dto.request.StoreUpdateRequest;
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
            @RequestParam(value = "sort", required = false, defaultValue = "") String sort,
            @RequestParam(value = "tab", required = false, defaultValue = "all") String tab,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        return ApiResponse.<PaginationResponse<StoreResponse>>builder()
                .result(storeService.getAllStores(page, size, tab, sort, search))
                .build();
    }

    @PatchMapping("/update-status/{storeId}")
    @Operation(summary = "Update store status by id", description = "Api update store status by id")
    public ApiResponse<Void> updateStoreStatus(@PathVariable String storeId) {
        storeService.changeStoreStatus(storeId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{storeId}")
    @Operation(summary = "Get one store by id", description = "Api get one store by id")
    public ApiResponse<StoreInformationResponse> getOneStoreById(@PathVariable String storeId) {
        return ApiResponse.<StoreInformationResponse>builder()
                .result(storeService.getOneStoreById(storeId))
                .build();
    }

    @GetMapping("/information")
    @Operation(summary = "Get one store by userId", description = "Api get one store by userId")
    public ApiResponse<StoreDetailResponse> getOneStoreByUserId() {
        return ApiResponse.<StoreDetailResponse>builder()
                .result(storeService.getOneStoreByUserId())
                .build();
    }

    @Operation(summary = "Register store", description = "Api register store")
    @PostMapping("/register-store")
    ApiResponse<StoreRegistrationResponse> registerStore(@RequestBody @Valid StoreRegistrationRequest request) {
        return ApiResponse.<StoreRegistrationResponse>builder()
                .result(storeService.registerStore(request))
                .build();
    }

    @Operation(summary = "Update store by userId", description = "Api update store by userId")
    @PutMapping("/update-store")
    ApiResponse<StoreUpdateResponse> updateStore(@RequestBody @Valid StoreUpdateRequest request) {
        return ApiResponse.<StoreUpdateResponse>builder()
                .result(storeService.updateStore(request))
                .build();
    }

    @Operation(summary = "Get all addresses by userId", description = "Api get all addresses by userId")
    @GetMapping("/addresses/seller")
    public ApiResponse<List<Map<String, Object>>> getAllAddressOfStore() {
        return ApiResponse.<List<Map<String, Object>>>builder()
                .result(storeService.getAllAddressOfStore())
                .build();
    }

    @DeleteMapping("/block/{storeId}")
    @Operation(summary = "Block store by id", description = "Api block store by id")
    public ApiResponse<Void> blockStore(@PathVariable String storeId) {
        storeService.blockStore(storeId);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/unblock/{storeId}")
    @Operation(summary = "Unblock store by id", description = "Api unblock store by id")
    public ApiResponse<Void> unblockStore(@PathVariable String storeId) {
        storeService.unblockStore(storeId);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Get store statistic", description = "Api get store statistic")
    @GetMapping("/statistic")
    public ApiResponse<StoreStatisticsResponse> getStoreStatistic() {
        return ApiResponse.<StoreStatisticsResponse>builder()
                .result(storeService.getStoreStatistic())
                .build();
    }

    @Operation(summary = "Check store online status", description = "Api check store online status")
    @GetMapping("/check_online_status/{storeId}")
    public ApiResponse<StoreCheckOnlineStatus> checkStoreOnlineStatus(@PathVariable String storeId) {
        return ApiResponse.<StoreCheckOnlineStatus>builder()
                .result(storeService.checkStoreOnlineStatus(storeId))
                .build();
    }
}
