package com.hkteam.ecommerce_platform.service;

import java.util.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.AddComponentRequest;
import com.hkteam.ecommerce_platform.dto.request.CategoryCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.CategoryUpdateRequest;
import com.hkteam.ecommerce_platform.dto.request.UpdateComponentRequest;
import com.hkteam.ecommerce_platform.dto.response.CategoryResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.entity.category.Category;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.enums.TypeSlug;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.CategoryMapper;
import com.hkteam.ecommerce_platform.repository.CategoryRepository;
import com.hkteam.ecommerce_platform.repository.ComponentRepository;
import com.hkteam.ecommerce_platform.util.PageUtils;
import com.hkteam.ecommerce_platform.util.SlugUtils;

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
    ComponentRepository componentRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        Category parentCategory = null;

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        if (request.getParentId() != null) {
            parentCategory = categoryRepository
                    .findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));
        }

        Category category = categoryMapper.toCategory(request);

        category.setSlug(SlugUtils.getSlug(request.getName(), TypeSlug.CATEGORY));
        category.setParent(parentCategory);

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while creating category: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Category parentCategory = null;

        boolean isDuplicateName = categoryRepository.existsByNameIgnoreCase(request.getName())
                && !category.getName().equalsIgnoreCase(request.getName());
        if (isDuplicateName) {
            throw new AppException(ErrorCode.CATEGORY_DUPLICATE);
        }

        if (request.getParentId() != null) {
            parentCategory = categoryRepository
                    .findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));
        }

        if (!category.getName().equalsIgnoreCase(request.getName())) {
            category.setSlug(SlugUtils.getSlug(request.getName(), TypeSlug.CATEGORY));
        }

        categoryMapper.updateCategoryFromRequest(request, category);

        category.setParent(parentCategory);

        try {
            categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while updating category: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    public PaginationResponse<CategoryResponse> getAllCategories(String pageStr, String sizeStr) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sort);

        var pageData = categoryRepository.findAll(pageable);
        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);

        return PaginationResponse.<CategoryResponse>builder()
                .currentPage(Integer.parseInt(pageStr))
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(pageData.getContent().stream()
                        .map(categoryMapper::toCategoryResponse)
                        .toList())
                .build();
    }

    public CategoryResponse getOneCategoryBySlug(String slug) {
        Category category =
                categoryRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse addComponentToCategory(Long categoryId, AddComponentRequest request) {
        var componentIds = request.getListComponent();

        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (componentIds == null || componentIds.isEmpty()) {
            throw new AppException(ErrorCode.LIST_COMPONENT_NOT_BLANK);
        }

        Set<Long> uniqueComponentIds = new HashSet<>(componentIds);
        if (uniqueComponentIds.size() < componentIds.size()) {
            throw new AppException(ErrorCode.DUPLICATE_COMPONENT_IDS);
        }

        for (Long componentId : uniqueComponentIds) {
            if (category.getComponents().stream()
                    .anyMatch(component -> (component.getId().equals(componentId))))
                throw new AppException(ErrorCode.COMPONENT_EXISTED_IN_CATE);
        }

        List<Component> components = componentRepository.findAllById(uniqueComponentIds);

        if (componentIds.size() != uniqueComponentIds.size()) {
            throw new AppException(ErrorCode.LIST_COMPONENT_NOT_FOUND);
        }

        category.getComponents().addAll(components);

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while adding component to the category: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateComponentToCategory(Long categoryId, UpdateComponentRequest request) {
        var componentIds = request.getListComponent();

        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (componentIds == null || componentIds.isEmpty()) {
            category.getComponents().clear();
            try {
                category = categoryRepository.save(category);
            } catch (DataIntegrityViolationException e) {
                log.info("Error while clearing components from category: {}", e.getMessage());
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }

            return categoryMapper.toCategoryResponse(category);
        }

        Set<Long> uniqueComponentIds = new HashSet<>(componentIds);
        if (uniqueComponentIds.size() < componentIds.size()) {
            throw new AppException(ErrorCode.DUPLICATE_COMPONENT_IDS);
        }

        List<Component> components = componentRepository.findAllById(uniqueComponentIds);

        if (components.size() != uniqueComponentIds.size()) {
            throw new AppException(ErrorCode.LIST_COMPONENT_NOT_FOUND);
        }

        category.getComponents().clear();

        category.getComponents().addAll(components);

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while updating components to category: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteOneComponentFromCategory(Long categoryId, Long componentId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Component component = componentRepository
                .findById(componentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));

        if (!category.getComponents().contains(component)) {
            throw new AppException(ErrorCode.COMPONENT_NOT_EXIST_IN_CATEGORY);
        }

        category.getComponents().remove(component);

        try {
            categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while deleting one component from category: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<CategoryResponse> getAll() {
        List categories;
        try {
            categories = categoryRepository.findAll(Sort.by("name").ascending()).stream()
                    .map(categoryMapper::toCategoryResponse)
                    .toList();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
        return categories;
    }
}
