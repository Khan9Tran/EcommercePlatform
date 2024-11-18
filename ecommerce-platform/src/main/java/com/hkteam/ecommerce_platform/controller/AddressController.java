package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.AddressCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.AddressUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.AddressResponse;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.UserAddressResponse;
import com.hkteam.ecommerce_platform.service.AddressService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Address Controller")
public class AddressController {
    AddressService addressService;

    @Operation(summary = "Create address", description = "Api create address")
    @PostMapping()
    public ApiResponse<AddressResponse> createAddress(@RequestBody @Valid AddressCreationRequest request) {
        AddressResponse addressResponse = addressService.createAddress(request);

        return ApiResponse.<AddressResponse>builder().result(addressResponse).build();
    }

    @Operation(summary = "Update address", description = "Api update address")
    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable Long id, @RequestBody @Valid AddressUpdateRequest request) {
        AddressResponse addressResponse = addressService.updateAddress(id, request);

        return ApiResponse.<AddressResponse>builder().result(addressResponse).build();
    }

    @Operation(summary = "Delete address", description = "Api delete address by id")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ApiResponse.<Void>builder()
                .message("Deleted address successfully")
                .build();
    }

    @Operation(summary = "Get all addresses", description = "Api get all addresses")
    @GetMapping()
    public ApiResponse<PaginationResponse<UserAddressResponse>> getAllAddresses(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size) {
        PaginationResponse<UserAddressResponse> paginationResponse = addressService.getAllAddresses(page, size);

        return ApiResponse.<PaginationResponse<UserAddressResponse>>builder()
                .result(paginationResponse)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one address by id", description = "Api get one address by id")
    public ApiResponse<AddressResponse> getOneAddressById(@PathVariable Long id) {
        AddressResponse addressResponse = addressService.getOneAddressById(id);

        return ApiResponse.<AddressResponse>builder().result(addressResponse).build();
    }
}
