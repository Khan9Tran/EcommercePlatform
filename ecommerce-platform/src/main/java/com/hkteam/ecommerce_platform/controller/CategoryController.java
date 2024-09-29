package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.AddComponentRequest;
import com.hkteam.ecommerce_platform.dto.request.CategoryCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.CategoryUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.CategoryResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Category Controller")
public class CategoryController {
    CategoryService categoryService;

    @Operation(summary = "Create category", description = "Api create category")
    @PostMapping()
    public ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryCreationRequest request) {
        CategoryResponse categoryResponse = categoryService.createCategory(request);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @Operation(summary = "Update category", description = "Api update category")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id, @RequestBody @Valid CategoryUpdateRequest request) {
        CategoryResponse categoryResponse = categoryService.updateCategory(id, request);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @Operation(summary = "Delete category", description = "Api delete category by id")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder().build();
    }

    @Operation(summary = "Get all categories", description = "Api get all categories")
    @GetMapping()
    public ApiResponse<PaginationResponse<CategoryResponse>> getAllCategories(
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size) {
        PaginationResponse<CategoryResponse> paginationResponse = categoryService.getAllCategories(page, size);

        return ApiResponse.<PaginationResponse<CategoryResponse>>builder()
                .result(paginationResponse)
                .build();
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get one category by slug", description = "Api get one category by slug")
    public ApiResponse<CategoryResponse> getOneCategoryBySlug(@PathVariable String slug) {
        CategoryResponse categoryResponse = categoryService.getOneCategoryBySlug(slug);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @Operation(summary = "Add components to category", description = "Api add components to a category")
    @PostMapping("/{categoryId}/components")
    public ApiResponse<CategoryResponse> addComponentToCategory(
            @PathVariable Long categoryId, @RequestBody @Valid AddComponentRequest addComponentRequest) {

        List<Long> componentIds = addComponentRequest.getListComponent();
        CategoryResponse categoryResponse = categoryService.addComponentToCategory(categoryId, componentIds);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }
}
