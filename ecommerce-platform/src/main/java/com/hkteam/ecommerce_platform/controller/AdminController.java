package com.hkteam.ecommerce_platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hkteam.ecommerce_platform.dto.response.AdminStatisticsResponse;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.RevenueOneYearResponse;
import com.hkteam.ecommerce_platform.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Admin Controller")
public class AdminController {
    AdminService adminService;

    @Operation(summary = "Get admin statistic", description = "Api get admin statistic")
    @GetMapping("/statistic")
    public ApiResponse<AdminStatisticsResponse> getStoreStatistic() {
        return ApiResponse.<AdminStatisticsResponse>builder()
                .result(adminService.getAdminStatistic())
                .build();
    }

    @Operation(summary = "Get revenue one year", description = "Api get revenue one year")
    @GetMapping("/revenue")
    public ApiResponse<RevenueOneYearResponse> getRevenueOneYear(
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "month", required = false) String month) {
        return ApiResponse.<RevenueOneYearResponse>builder()
                .result(adminService.getRevenueOneYear(year, month))
                .build();
    }
}
