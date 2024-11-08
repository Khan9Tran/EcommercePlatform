package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
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
    @GetMapping()
    public ApiResponse<Object> getCartList(String page, String size) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCart() {
        return null;
    }

}
