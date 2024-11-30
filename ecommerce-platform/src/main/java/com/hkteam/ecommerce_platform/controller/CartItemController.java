package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.CartItemCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.CartItemUpdateQuantityRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.CartItemResponse;
import com.hkteam.ecommerce_platform.dto.response.MiniCartItemResponse;
import com.hkteam.ecommerce_platform.dto.response.QuantityCartItemsResponse;
import com.hkteam.ecommerce_platform.service.CartItemService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/cartItems")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "CartItem Controller")
public class CartItemController {
    CartItemService cartItemService;

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCartItem(@PathVariable Long id) {
        cartItemService.deleteCartItem(id);
        return ApiResponse.<Void>builder()
                .message("Deleted cart item successfully")
                .build();
    }

    @PutMapping("/{id}/change-quantity")
    public ApiResponse<CartItemResponse> changeQuantity(
            @RequestBody CartItemUpdateQuantityRequest request, @PathVariable Long id) {
        return ApiResponse.<CartItemResponse>builder()
                .result(cartItemService.changeQuantity(request, id))
                .build();
    }

    @PostMapping("")
    public ApiResponse<CartItemResponse> addCartItem(@RequestBody @Valid CartItemCreationRequest request) {

        return ApiResponse.<CartItemResponse>builder()
                .result(cartItemService.addProductToCart(request))
                .build();
    }

    @GetMapping("/count")
    public ApiResponse<QuantityCartItemsResponse> countCartItems() {
        return ApiResponse.<QuantityCartItemsResponse>builder()
                .result(cartItemService.countCartItems())
                .build();
    }

    @GetMapping("/top5")
    public ApiResponse<List<MiniCartItemResponse>> getTop5CartItems() {
        return ApiResponse.<List<MiniCartItemResponse>>builder()
                .result(cartItemService.getCartItemNewest())
                .build();
    }
}
