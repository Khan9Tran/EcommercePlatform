package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.DefaultAddressRequest;
import com.hkteam.ecommerce_platform.dto.request.UserCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.UserDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.UserResponse;
import com.hkteam.ecommerce_platform.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "User Controller")
public class UserController {
    UserService userService;

    @Operation(summary = "Create user", description = "Api create new user")
    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUsers(request))
                .build();
    }

    @Operation(summary = "Get user by id", description = "Api get user by id")
    @GetMapping("/{userId}")
    UserDetailResponse getUser(@PathVariable("userId") String userId) {
        return userService.getUser(userId);
    }

    @Operation(summary = "Set default address for user", description = "Api set default address for user")
    @PostMapping("/default-address")
    ApiResponse<Void> setDefaultAddress(@RequestBody @Valid DefaultAddressRequest request) {
        userService.setDefaultAddress(request);
        return ApiResponse.<Void>builder().build();
    }
}
