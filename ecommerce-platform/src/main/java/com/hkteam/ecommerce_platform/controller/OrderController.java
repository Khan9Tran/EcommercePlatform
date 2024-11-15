package com.hkteam.ecommerce_platform.controller;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.OrderRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
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

    @GetMapping("/{orderId}")
    @Operation(summary = "Get one order by id", description = "Api get one order by id")
    public ApiResponse<OrderResponse> getOneOrderById(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOneOrderById(orderId))
                .build();
    }

    @Operation(summary = "Get all order by seller", description = "Api get all order by seller")
    @GetMapping("/seller")
    public ApiResponse<PaginationResponse<OrderResponse>> getAllOrderBySeller(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        return ApiResponse.<PaginationResponse<OrderResponse>>builder()
                .result(orderService.getAllOrderBySeller(page, size, sortBy, orderBy, search))
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
                .message("Cancelled order successfully")
                .build();
    }

    @PostMapping("/")
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(orderRequest))
                .build();
    }
}
