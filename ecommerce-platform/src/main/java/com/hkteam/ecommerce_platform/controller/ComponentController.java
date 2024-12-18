package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ComponentCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ComponentUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.ComponentResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.service.ComponentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/components")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Component Controller")
public class ComponentController {
    ComponentService componentService;

    @Operation(summary = "Create component", description = "Api create component")
    @PostMapping()
    public ApiResponse<ComponentResponse> createComponent(@RequestBody @Valid ComponentCreationRequest request) {
        ComponentResponse componentResponse = componentService.createComponent(request);

        return ApiResponse.<ComponentResponse>builder()
                .result(componentResponse)
                .build();
    }

    @Operation(summary = "Update component", description = "Api update component")
    @PutMapping("/{id}")
    public ApiResponse<ComponentResponse> updateComponent(
            @PathVariable Long id, @RequestBody @Valid ComponentUpdateRequest request) {

        ComponentResponse componentResponse = componentService.updateComponent(id, request);

        return ApiResponse.<ComponentResponse>builder()
                .result(componentResponse)
                .build();
    }

    @Operation(summary = "Delete component", description = "Api delete component by id")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComponent(@PathVariable Long id) {
        componentService.deleteComponent(id);
        return ApiResponse.<Void>builder()
                .message("Deleted component successfully")
                .build();
    }

    @Operation(summary = "Get all components", description = "Api get all components")
    @GetMapping()
    public ApiResponse<PaginationResponse<ComponentResponse>> getAllComponents(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        PaginationResponse<ComponentResponse> paginationResponse =
                componentService.getAllComponents(page, size, search);

        return ApiResponse.<PaginationResponse<ComponentResponse>>builder()
                .result(paginationResponse)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one component by id", description = "Api get one component by id")
    public ApiResponse<ComponentResponse> getOneComponentById(@PathVariable Long id) {
        ComponentResponse componentResponse = componentService.getOneComponentById(id);

        return ApiResponse.<ComponentResponse>builder()
                .result(componentResponse)
                .build();
    }

    @Operation(summary = "Get all components", description = "Api get all components")
    @GetMapping("/all")
    public ApiResponse<List<ComponentResponse>> getAllComponents() {
        var result = componentService.getAllComponents();

        return ApiResponse.<List<ComponentResponse>>builder().result(result).build();
    }

    @Operation(summary = "Get all components by category id", description = "Api get all components by category id")
    @GetMapping("/category/{id}")
    public ApiResponse<List<ComponentResponse>> getAllComponentsByCategoryId(@PathVariable Long id) {
        var result = componentService.getAllComponentsByCategoryId(id);

        return ApiResponse.<List<ComponentResponse>>builder().result(result).build();
    }
}
