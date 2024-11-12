package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.CartResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Cart Controller")
public class CartController {
    CartService cartService;

    @GetMapping()
    public ApiResponse<PaginationResponse<CartResponse>> getCartList(@RequestParam(value = "page", required = false, defaultValue = "1") String page,
                                                                     @RequestParam(value = "size", required = false, defaultValue = "10") String size
    ) {
        return ApiResponse.<PaginationResponse<CartResponse>>builder().result(cartService.getCarts(page, size)).build();

    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ApiResponse.<Void>builder().build();
    }

}
