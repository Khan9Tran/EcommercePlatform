package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.request.CartItemCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.CartItemResponse;
import com.hkteam.ecommerce_platform.service.CartItemService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cartItems")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "CartItem Controller")
public class CartItemController {
    CartItemService cartItemService;

    @DeleteMapping("/id")
    public ApiResponse<Void> deleteCartItem(@PathVariable Long id) {
        return null;
    }

//    @PutMapping("/{id}")
//    public  ApiResponse<Void> changeQuantity(@PathVariable Long id, Integer newQuantity) {
//        return null;
//    }

    @PostMapping("")
    public  ApiResponse<CartItemResponse> addCartItem(@RequestBody @Valid CartItemCreationRequest request) {

        return  ApiResponse.<CartItemResponse>builder()
                .result(cartItemService.addProductToCart(request))
                .build();
    }
}
