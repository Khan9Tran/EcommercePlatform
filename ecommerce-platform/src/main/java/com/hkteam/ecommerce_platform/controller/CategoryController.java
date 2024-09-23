package com.hkteam.ecommerce_platform.controller;

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

    @Operation(summary = "Create category", description = "Api create new category")
    @PostMapping(consumes = {"multipart/form-data"})
    public ApiResponse<CategoryResponse> createCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam("imageUrl") MultipartFile imageUrl,
            @RequestParam("iconUrl") MultipartFile iconUrl) {

        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .imageUrl(imageUrl)
                .iconUrl(iconUrl)
                .build();

        CategoryResponse categoryResponse = categoryService.createCategory(request);

        return ApiResponse.<CategoryResponse>builder().result(categoryResponse).build();
    }

    @Operation(summary = "Update category", description = "Api update existing category")
    @PutMapping(
            value = "/{id}",
            consumes = {"multipart/form-data"})
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam(value = "imageUrl", required = false) MultipartFile imageUrl,
            @RequestParam(value = "iconUrl", required = false) MultipartFile iconUrl) {

        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .imageUrl(imageUrl)
                .iconUrl(iconUrl)
                .build();

        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .build();
    }

    @Operation(summary = "Delete category", description = "Api delete category by id")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<String>builder()
                .result("Category deleted successfully")
                .build();
    }

    @Operation(summary = "Get one category", description = "Api get category by id")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getOneCategory(@PathVariable Long id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getOneCategory(id))
                .build();
    }

    @Operation(summary = "Get all categories", description = "Api get all categories")
    @GetMapping()
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }
}
