package com.hkteam.ecommerce_platform.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.*;
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
    @Operation(summary = "Update one order status by seller", description = "Api update one order status by seller")
    public ApiResponse<Void> updateOneOrderStatusBySeller(@PathVariable String orderId) {
        orderService.updateOneOrderStatusBySeller(orderId);
        return ApiResponse.<Void>builder()
                .message("Updated status order by seller successfully")
                .build();
    }

    @PutMapping("/list/update-status/seller")
    @Operation(summary = "Update list order status by seller", description = "Api update list order status by seller")
    public ApiResponse<Void> updateListOrderStatusBySeller(
            @RequestBody @Valid OrderListStatusUpdateSellerRequest request) {
        orderService.updateListOrderStatusBySeller(request.getListOrderId());
        return ApiResponse.<Void>builder()
                .message("Updated status list order by seller successfully")
                .build();
    }

    @PutMapping("/{orderId}/cancel/seller")
    @Operation(summary = "Cancel one order by seller", description = "Api cancel one order by seller")
    public ApiResponse<Void> cancelOneOrderBySeller(@PathVariable String orderId) {
        orderService.cancelOneOrderBySeller(orderId);
        return ApiResponse.<Void>builder()
                .message("Cancelled order by seller successfully")
                .build();
    }

    @PutMapping("/list/cancel/seller")
    @Operation(summary = "Cancel list order by seller", description = "Api cancel list order by seller")
    public ApiResponse<Void> cancelListOrderBySeller(@RequestBody @Valid OrderListCancelSellerRequest request) {
        orderService.cancelListOrderBySeller(request.getListOrderId());
        return ApiResponse.<Void>builder()
                .message("Cancelled list order by seller successfully")
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
    @Operation(summary = "Update one order status by admin", description = "Api update one order status by admin")
    public ApiResponse<Void> updateOneOrderStatusByAdmin(@PathVariable String orderId) {
        orderService.updateOneOrderStatusByAdmin(orderId);
        return ApiResponse.<Void>builder()
                .message("Updated status order by admin successfully")
                .build();
    }

    @PutMapping("/list/update-status/admin")
    @Operation(summary = "Update list order status by admin", description = "Api update list order status by admin")
    public ApiResponse<Void> updateListOrderStatusByAdmin(
            @RequestBody @Valid OrderListStatusUpdateAdminRequest request) {
        orderService.updateListOrderStatusByAdmin(request.getListOrderId());
        return ApiResponse.<Void>builder()
                .message("Updated status list order by admin successfully")
                .build();
    }

    @PutMapping("/{orderId}/cancel/admin")
    @Operation(summary = "Cancel one order by admin", description = "Api cancel one order by admin")
    public ApiResponse<Void> cancelOneOrderByAdmin(@PathVariable String orderId) {
        orderService.cancelOneOrderByAdmin(orderId);
        return ApiResponse.<Void>builder()
                .message("Cancelled order by admin successfully")
                .build();
    }

    @PutMapping("/list/cancel/admin")
    @Operation(summary = "Cancel list order by admin", description = "Api cancel list order by admin")
    public ApiResponse<Void> cancelListOrderByAdmin(@RequestBody @Valid OrderListCancelAdminRequest request) {
        orderService.cancelListOrderByAdmin(request.getListOrderId());
        return ApiResponse.<Void>builder()
                .message("Cancelled list order by admin successfully")
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
    @Operation(summary = "Cancel one order by user", description = "Api cancel one order by user")
    public ApiResponse<Void> cancelOneOrderByUser(@PathVariable String orderId) {
        orderService.cancelOneOrderByUser(orderId);
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
