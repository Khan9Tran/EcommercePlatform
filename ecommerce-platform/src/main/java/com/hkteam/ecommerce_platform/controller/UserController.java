package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.*;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    ApiResponse<UserDetailResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserDetailResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @Operation(summary = "Get all user", description = "Api get list user with page and size")
    @GetMapping
    ApiResponse<PaginationResponse<UserResponse>> getAllUsers(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size) {
        return ApiResponse.<PaginationResponse<UserResponse>>builder()
                .result(userService.getAllUsers(page, size))
                .build();
    }

    @Operation(summary = "Set default address for user", description = "Api set default address for user")
    @PutMapping("/default-address")
    ApiResponse<Void> setDefaultAddress(@RequestBody @Valid DefaultAddressRequest request) {
        userService.setDefaultAddress(request);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Delete user by id", description = "Api delete user by id")
    @DeleteMapping("/{userId}")
    ApiResponse<Void> deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Update user by id", description = "Api update user by id")
    @PutMapping("/{userId}")
    ApiResponse<UserUpdateResponse> updateUser(
            @PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserUpdateResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @Operation(summary = "My information", description = "Api get my information")
    @GetMapping("/me")
    ApiResponse<UserDetailResponse> getMyInformation() {
        return ApiResponse.<UserDetailResponse>builder()
                .result(userService.getMyInformation())
                .build();
    }

    @Operation(summary = "Create password", description = "Api for create password")
    @PutMapping("/create-password")
    ApiResponse<Void> createPassword(@RequestBody @Valid PasswordCreationRequest request) {
        userService.createPassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Update password", description = "Api for update password")
    @PutMapping("/update-password")
    ApiResponse<Void> updatePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userService.updatePassword(request);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "get customer", description = "Api get customer")
    @GetMapping("/customers")
    ApiResponse<PaginationResponse<CustomerResponse>> getAllCustomers(
            @RequestParam(value="date", required = false, defaultValue = "") String sortDate,
            @RequestParam(value="name", required = false, defaultValue = "") String sortName,
            @RequestParam(value="tab", required = false, defaultValue = "all") String tab,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size) {
        return ApiResponse.<PaginationResponse<CustomerResponse>>builder()
                .result(userService.getAllCustomers(page, size, tab, sortDate, sortName))
                .build();
    }


    @PutMapping("/customers")
    ApiResponse<Void> changeStatusAccount( @RequestBody @Valid UserAccountRequest request){
        userService.changeStatusAccount(request);
        return ApiResponse.<Void>builder()
                .build();

    }

}
