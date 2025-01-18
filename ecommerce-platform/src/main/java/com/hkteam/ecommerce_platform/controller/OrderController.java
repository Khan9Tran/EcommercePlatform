package com.hkteam.ecommerce_platform.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ListOrder;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Order Controller")
public class OrderController {
    OrderService orderService;

    @GetMapping("/{orderId}/seller")
    @Operation(summary = "Get one order by seller", description = "Api get one order by seller")
    public ApiResponse<OrderDetailSellerResponse> getOneOrderBySeller(@PathVariable String orderId) {
        return ApiResponse.<OrderDetailSellerResponse>builder()
                .result(orderService.getOneOrderBySeller(orderId))
                .build();
    }

    @Operation(summary = "Get all order by seller", description = "Api get all order by seller")
    @GetMapping("/seller")
    public ApiResponse<PaginationResponse<OrderGetAllSellerResponse>> getAllOrderBySeller(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "orderBy", required = false, defaultValue = "") String orderBy,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "filter", required = false, defaultValue = "") String filter) {
        return ApiResponse.<PaginationResponse<OrderGetAllSellerResponse>>builder()
                .result(orderService.getAllOrderBySeller(page, size, sortBy, orderBy, search, filter))
                .build();
    }

    @PutMapping("/{orderId}/update-status/seller")
    @Operation(summary = "Update order status by seller", description = "Api update status order by seller")
    public ApiResponse<Void> updateOrderStatusBySeller(@PathVariable String orderId) {
        orderService.updateOrderStatusBySeller(orderId);
        return ApiResponse.<Void>builder()
                .message("Updated status order successfully")
                .build();
    }

    @PutMapping("/{orderId}/cancel/seller")
    @Operation(summary = "Cancel order by seller", description = "Api cancel order by seller")
    public ApiResponse<Void> cancelOrderBySeller(@PathVariable String orderId) {
        orderService.cancelOrderBySeller(orderId);
        return ApiResponse.<Void>builder()
                .message("Cancelled order by seller successfully")
                .build();
    }

    @GetMapping("/{orderId}/admin")
    @Operation(summary = "Get one order by admin", description = "Api get one order by admin")
    public ApiResponse<OrderDetailAdminResponse> getOneOrderByAdmin(@PathVariable String orderId) {
        return ApiResponse.<OrderDetailAdminResponse>builder()
                .result(orderService.getOneOrderByAdmin(orderId))
                .build();
    }

    @Operation(summary = "Get all order by admin", description = "Api get all order by admin")
    @GetMapping("/admin")
    public ApiResponse<PaginationResponse<OrderGetAllAdminResponse>> getAllOrderByAdmin(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "orderBy", required = false, defaultValue = "") String orderBy,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "filter", required = false, defaultValue = "") String filter) {
        return ApiResponse.<PaginationResponse<OrderGetAllAdminResponse>>builder()
                .result(orderService.getAllOrderByAdmin(page, size, sortBy, orderBy, search, filter))
                .build();
    }

    @PutMapping("/{orderId}/update-status/admin")
    @Operation(summary = "Update order status by admin", description = "Api update status order by admin")
    public ApiResponse<Void> updateOrderStatusByAdmin(@PathVariable String orderId) {
        orderService.updateOrderStatusByAdmin(orderId);
        return ApiResponse.<Void>builder()
                .message("Updated status order successfully")
                .build();
    }

    @PutMapping("/{orderId}/cancel/admin")
    @Operation(summary = "Cancel order by admin", description = "Api cancel order by admin")
    public ApiResponse<Void> cancelOrderByAdmin(@PathVariable String orderId) {
        orderService.cancelOrderByAdmin(orderId);
        return ApiResponse.<Void>builder()
                .message("Cancelled order by admin successfully")
                .build();
    }

    @GetMapping("/{orderId}/user")
    @Operation(summary = "Get one order by user", description = "Api get one order by user")
    public ApiResponse<OrderGetOneUserResponse> getOneOrderByUser(@PathVariable String orderId) {
        return ApiResponse.<OrderGetOneUserResponse>builder()
                .result(orderService.getOneOrderByUser(orderId))
                .build();
    }

    @Operation(summary = "Get all order by user", description = "Api get all order by user")
    @GetMapping("/user")
    public ApiResponse<PaginationResponse<OrderGetAllUserResponse>> getAllOrderByUser(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "orderBy", required = false, defaultValue = "") String orderBy,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "filter", required = false, defaultValue = "") String filter) {
        return ApiResponse.<PaginationResponse<OrderGetAllUserResponse>>builder()
                .result(orderService.getAllOrderByUser(page, size, sortBy, orderBy, search, filter))
                .build();
    }

    @PutMapping("/{orderId}/cancel/user")
    @Operation(summary = "Cancel order by user", description = "Api cancel order by user")
    public ApiResponse<Void> cancelOrderByUser(@PathVariable String orderId) {
        orderService.cancelOrderByUser(orderId);
        return ApiResponse.<Void>builder()
                .message("Cancelled order by user successfully")
                .build();
    }

    @PostMapping("/")
    public ApiResponse<OrderCreationResponse> createOrder(
            @RequestBody ListOrder listOrder, HttpServletRequest request) {
        return ApiResponse.<OrderCreationResponse>builder()
                .result(orderService.createOrder(listOrder, request))
                .build();
    }
}
