package com.hkteam.ecommerce_platform.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.request.CategoryCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.CategoryResponse;
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
    @PostMapping(consumes = {"multipart/form-data"})
    public ApiResponse<CategoryResponse> createCategory(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "imageUrl") MultipartFile imageUrl,
            @RequestParam(value = "iconUrl") MultipartFile iconUrl) {

        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .imageUrl(imageUrl)
                .iconUrl(iconUrl)
                .build();

        CategoryResponse categoryResponse = categoryService.createCategory(request);

        return ApiResponse.<CategoryResponse>builder()
                .result(categoryResponse)
                .message("Create category successfully!")
                .code(1000)
                .build();
    }

    @Operation(summary = "Update category", description = "Api update category")
    @PutMapping(
            value = "/{id}",
            consumes = {"multipart/form-data"})
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "imageUrl", required = false) MultipartFile imageUrl,
            @RequestParam(value = "iconUrl", required = false) MultipartFile iconUrl)
            throws IOException {

        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .imageUrl(imageUrl)
                .iconUrl(iconUrl)
                .build();

        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .message("Update category successfully!")
                .code(1000)
                .build();
    }

    @Operation(summary = "Delete category", description = "Api delete category by id")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<String>builder()
                .message("Category deleted successfully")
                .code(1000)
                .build();
    }

    @Operation(summary = "Get all categories", description = "Api get all categories")
    @GetMapping()
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .message("Get all categories successfully!")
                .code(1000)
                .build();
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get one category by slug", description = "Api get one category by slug")
    public ApiResponse<CategoryResponse> getOneCategoryBySlug(@PathVariable String slug) {
        CategoryResponse categoryResponse = categoryService.getOneCategoryBySlug(slug);
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryResponse)
                .message("Get one category by slug successfully!")
                .code(1000)
                .build();
    }
}
