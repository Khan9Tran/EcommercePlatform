package com.hkteam.ecommerce_platform.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.repository.ComponentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.CategoryCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.CategoryResponse;
import com.hkteam.ecommerce_platform.entity.category.Category;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.CategoryMapper;
import com.hkteam.ecommerce_platform.repository.CategoryRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryService {
    CategoryMapper categoryMapper;
    CategoryRepository categoryRepository;
    CloudinaryService cloudinaryService;
    ComponentRepository componentRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        log.info("Start creating category with name: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Category with name {} already exists.", request.getName());
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        Category parentCategory = null;
        if (request.getParentId() != null) {
            log.info("Checking for parent category with ID: {}", request.getParentId());
            parentCategory = categoryRepository
                    .findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));
        }

        log.info("Uploading image for category...");
        Map imageData = cloudinaryService.uploadImage(request.getImageUrl(), "ecommerce-be/category");
        log.info("Image uploaded with URL: {}", imageData.get("url"));

        log.info("Uploading icon for category...");
        Map iconData = cloudinaryService.uploadImage(request.getIconUrl(), "ecommerce-be/category");
        log.info("Icon uploaded with URL: {}", iconData.get("url"));

        Category category = categoryMapper.toCategory(request);

        category.setSlug(generateSlug(category.getName()));
        category.setImageUrl((String) imageData.get("url"));
        category.setIconUrl((String) iconData.get("url"));
        category.setParent(parentCategory);
        log.debug("Category details before saving: {}", category);

        try {
            category = categoryRepository.save(category);
            log.info("Category saved successfully with ID: {}", category.getId());
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving category: {}", e.getMessage());
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        log.info("Category created successfully: {}", response);

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryCreationRequest request) throws IOException {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new AppException(ErrorCode.NAME_NOT_BLANK);
        }

        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByName(request.getName())
                && !category.getName().equals(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_DUPLICATE);
        }
        category.setName(request.getName());

        if (!category.getName().equals(request.getName())) {
            category.setSlug(generateSlug(request.getName()));
        }

        category.setDescription(request.getDescription());

        if (request.getParentId() != null) {
            Category parentCategory = categoryRepository
                    .findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));
            category.setParent(parentCategory);
        } else {
            category.setParent(null);
        }

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            log.info("Deleting old image URL: {}", category.getImageUrl());
            cloudinaryService.deleteImage(category.getImageUrl());
            Map imageData = cloudinaryService.uploadImage(request.getImageUrl(), "ecommerce-be/category");
            category.setImageUrl((String) imageData.get("url"));
        }

        if (request.getIconUrl() != null && !request.getIconUrl().isEmpty()) {
            log.info("Deleting old icon URL: {}", category.getIconUrl());
            cloudinaryService.deleteImage(category.getIconUrl());
            Map iconData = cloudinaryService.uploadImage(request.getIconUrl(), "ecommerce-be/category");
            category.setIconUrl((String) iconData.get("url"));
        }

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(categoryMapper::toCategoryResponse).collect(Collectors.toList());
    }

    public CategoryResponse getOneCategoryBySlug(String slug) {
        Category category =
                categoryRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    public String generateSlug(String name) {
        String slug;
        do {
            slug = name.trim();
            slug = slug.replaceAll("[^\\p{L}\\p{N}\\s-]", "");
            slug = slug.replaceAll("\\s+", "-");
            slug = slug.replaceAll("-+", "-");
            slug = slug.replaceAll("^-|-$", "");
            String randomCode = generateNumericCode();
            slug = slug + "-cate." + randomCode;
        } while (categoryRepository.existsBySlug(slug));
        return slug;
    }

    private String generateNumericCode() {
        Random random = new Random();
        StringBuilder numericCode = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            int digit = random.nextInt(10);
            numericCode.append(digit);
        }

        return numericCode.toString();
    }

    public CategoryResponse addComponentToCategory(Long categoryId, List<Long> componentIds) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        List<Component> components = componentRepository.findAllById(componentIds);

        if (components.size() != componentIds.size()) {
            throw new AppException(ErrorCode.LIST_COMPONENT_NOT_FOUND);
        }

        category.getComponents().addAll(components);
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toCategoryResponse(savedCategory);
    }
}
