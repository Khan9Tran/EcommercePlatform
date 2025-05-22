package com.hkteam.ecommerce_platform.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.AddComponentRequest;
import com.hkteam.ecommerce_platform.dto.request.CategoryCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.CategoryUpdateRequest;
import com.hkteam.ecommerce_platform.dto.request.UpdateComponentRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
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
    @CacheEvict(value = "categoriesTreeCache", allEntries = true)
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id, @RequestBody @Valid CategoryUpdateRequest request) {
        CategoryResponse categoryResponse = categoryService.updateCategory(id, request);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @GetMapping("/with-id/{id}")
    public ApiResponse<CategoryFilterResponse> getCategory(@PathVariable Long id) {
        log.info("Get category by id: {}", id);
        return ApiResponse.<CategoryFilterResponse>builder()
                .result(categoryService.getCategory(id))
                .build();
    }

    @Operation(summary = "Delete category", description = "Api delete category by id")
    @CacheEvict(value = "categoriesTreeCache", allEntries = true)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder()
                .message("Deleted category successfully")
                .build();
    }

    @Operation(summary = "Get all categories", description = "Api get all categories")
    @GetMapping()
    public ApiResponse<PaginationResponse<CategoryResponse>> getAllCategories(
            @RequestParam(value = "sort", required = false, defaultValue = "") String sort,
            @RequestParam(value = "tab", required = false, defaultValue = "all") String tab,
            @RequestParam(value = "page", required = false, defaultValue = "1") String page,
            @RequestParam(value = "size", required = false, defaultValue = "10") String size,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        log.info(
                "Get all categories with tab: {}, sort: {}, page: {}, size: {}, search: {}",
                tab,
                sort,
                page,
                size,
                search);
        return ApiResponse.<PaginationResponse<CategoryResponse>>builder()
                .result(categoryService.getAllCategories(page, size, tab, sort, search))
                .build();
    }

    @GetMapping("/{slug}")
    @Cacheable(value = "categoryCache", key = "#slug", unless = "#result == null")
    @Operation(summary = "Get one category by slug", description = "Api get one category by slug")
    public ApiResponse<CategoryResponse> getOneCategoryBySlug(@PathVariable String slug) {
        log.info("Get category by slug: {}", slug);
        CategoryResponse categoryResponse = categoryService.getOneCategoryBySlug(slug);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @Operation(summary = "Add components to category", description = "Api add components to a category")
    @PostMapping("/{categoryId}/components")
    public ApiResponse<CategoryResponse> addComponentToCategory(
            @PathVariable Long categoryId, @RequestBody @Valid AddComponentRequest addComponentRequest) {

        var categoryResponse = categoryService.addComponentToCategory(categoryId, addComponentRequest);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @PutMapping("/{categoryId}/components")
    @Operation(summary = "Update components to category", description = "API update the components to category")
    public ApiResponse<CategoryResponse> updateComponentToCategory(
            @PathVariable Long categoryId, @RequestBody @Valid UpdateComponentRequest request) {

        var categoryResponse = categoryService.updateComponentToCategory(categoryId, request);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @DeleteMapping("/{categoryId}/components/{componentId}")
    @Operation(summary = "Delete one component from category", description = "API delete one component from category")
    public ApiResponse<Void> deleteOneComponentFromCategory(
            @PathVariable Long categoryId, @PathVariable Long componentId) {
        categoryService.deleteOneComponentFromCategory(categoryId, componentId);
        return ApiResponse.<Void>builder()
                .message("One component deleted from category successfully")
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<CategoryResponse>> findAll() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAll())
                .build();
    }

    @Cacheable(value = "categoriesTreeCache", key = "'treeViewKey'", unless = "#result == null")
    @GetMapping(("/tree-view"))
    public ApiResponse<List<CategoryTreeViewResponse>> getTreeView() {
        return ApiResponse.<List<CategoryTreeViewResponse>>builder()
                .result(categoryService.getTreeView())
                .build();
    }


    @GetMapping("/detail-component")
    public ApiResponse<List<CateHasComponentResponse>> getAllCateHasComponent(@RequestParam List<Long> categoryIds) {
        return ApiResponse.<List<CateHasComponentResponse>>builder()
                .result(categoryService.getAllCateHasComponent(categoryIds))
                .build();
    }
}
