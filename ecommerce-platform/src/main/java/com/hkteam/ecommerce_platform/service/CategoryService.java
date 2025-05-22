package com.hkteam.ecommerce_platform.service;

import java.util.*;

import com.hkteam.ecommerce_platform.dto.response.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.*;
import com.hkteam.ecommerce_platform.entity.category.Category;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.enums.TypeSlug;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.CategoryMapper;
import com.hkteam.ecommerce_platform.rabbitmq.RabbitMQConfig;
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
    RabbitTemplate rabbitTemplate;
    CacheManager cacheManager;

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
            throw new AppException(ErrorCode.CATEGORY_LATER_EXISTED);
        }

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Category parentCategory = null;

        boolean isDuplicateName = categoryRepository.existsByNameIgnoreCase(request.getName())
                && !category.getName().equalsIgnoreCase(request.getName())
                && !category.isDeleted();
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
        String oldName = category.getName();
        categoryMapper.updateCategoryFromRequest(request, category);

        category.setParent(parentCategory);

        try {
            categoryRepository.save(category);
            if (!oldName.equals(category.getName())) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.CATE_ES_PRODUCT_QUEUE,
                        UpdateCategoryEsProductRequest.builder()
                                .isDeleted(Boolean.FALSE)
                                .name(category.getName())
                                .id(id)
                                .build());
            }
        } catch (DataIntegrityViolationException e) {
            log.info("Error while updating category: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        Objects.requireNonNull(cacheManager.getCache("categoryCache")).evict(category.getSlug());
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        try {
            categoryRepository.delete(category);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CATE_ES_PRODUCT_QUEUE,
                    UpdateCategoryEsProductRequest.builder()
                            .isDeleted(Boolean.TRUE)
                            .id(id)
                            .build());
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
        Objects.requireNonNull(cacheManager.getCache("categoryCache")).evict(category.getSlug());
    }

    public PaginationResponse<CategoryResponse> getAllCategories(
            String pageStr, String sizeStr, String tab, String sort, String search) {
        Sort sortable =
                switch (sort) {
                    case "newest" -> Sort.by("createdAt").descending();
                    case "oldest" -> Sort.by("createdAt").ascending();
                    case "az" -> Sort.by("name").ascending();
                    case "za" -> Sort.by("name").descending();
                    default -> Sort.unsorted();
                };

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        var pageData = categoryRepository.searchAllCategory(search, pageable);

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

    public List<CategoryTreeViewResponse> getTreeView() {
        var categories = categoryRepository.findByParentNull();
        log.info("categories: {}", categories);
        List<CategoryTreeViewResponse> categoryTreeViewResponses = new ArrayList<>();
        for (Category category : categories) {
            var categoryTreeViewResponse = categoryMapper.toCategoryTreeViewResponse(category);
            categoryTreeViewResponse.setChildren(getChildren(category));
            categoryTreeViewResponses.add(categoryTreeViewResponse);
        }

        return categoryTreeViewResponses;
    }

    private List<CategoryTreeViewResponse> getChildren(Category category) {
        if (Objects.isNull(category.getChildren()) || category.getChildren().isEmpty()) {
            return null;
        }
        List<CategoryTreeViewResponse> categoryTreeViewResponses = new ArrayList<>();

        var categories = category.getChildren().stream().toList();
        for (Category cate : categories) {
            var cateTreeView = categoryMapper.toCategoryTreeViewResponse(cate);
            cateTreeView.setChildren(getChildren(cate));
            categoryTreeViewResponses.add(cateTreeView);
        }

        return categoryTreeViewResponses;
    }

    public CategoryFilterResponse getCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        return categoryMapper.toCategoryFilterResponse(category);
    }

    public List<CateHasComponentResponse> getAllCateHasComponent(List<Long> categoryIds) {
        var rs = categoryRepository.findAllById(categoryIds);
        log.info(rs.toString());
        List<CateHasComponentResponse> cateHasComponentResponses = new ArrayList<>();
        for (Category category : rs) {
            List<ComponentDetailResponse> componentDetailResponses = new ArrayList<>();
            for (Component component : category.getComponents()) {
                var componentDetailResponse = ComponentDetailResponse.builder()
                        .id(component.getId())
                        .name(component.getName())
                        .categoryId(category.getId())
                        .build();
                for (var values: component.getProductComponentValues()) {
                    if (componentDetailResponse.getValues() == null) {
                        componentDetailResponse.setValues(new ArrayList<>());
                    }
                    var productComponentValueResponse = ProductComponentValueResponse.builder()
                            .id(values.getId())
                            .value(values.getValue())
                            .build();
                    componentDetailResponse.getValues().add(productComponentValueResponse);
                }
                componentDetailResponses.add(componentDetailResponse);
            }
            var cateHasComponentResponse = CateHasComponentResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .components(componentDetailResponses)
                    .build();
            cateHasComponentResponses.add(cateHasComponentResponse);
        }

        return cateHasComponentResponses;
    }
}
