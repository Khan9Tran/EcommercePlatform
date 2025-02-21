package com.hkteam.ecommerce_platform.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ViewProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ViewProductCreationResponse;
import com.hkteam.ecommerce_platform.service.ViewProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/view_product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "View Product Controller")
public class ViewProductController {
    ViewProductService viewProductService;

    @Operation(summary = "Create view product", description = "Api create view product")
    @PostMapping()
    ApiResponse<ViewProductCreationResponse> createViewProduct(@RequestBody @Valid ViewProductCreationRequest request) {
        return ApiResponse.<ViewProductCreationResponse>builder()
                .result(viewProductService.createViewProduct(request))
                .build();
    }

    @Operation(summary = "Change count view product", description = "Api change count view product")
    @PutMapping("/change_count/{productId}")
    ApiResponse<Void> changeCountViewProduct(@PathVariable String productId) {
        viewProductService.changeCountViewProduct(productId);
        return ApiResponse.<Void>builder()
                .message("Changed successfully count view product")
                .build();
    }
}
