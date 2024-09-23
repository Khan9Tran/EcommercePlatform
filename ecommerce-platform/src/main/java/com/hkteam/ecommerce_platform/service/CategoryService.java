package com.hkteam.ecommerce_platform.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        Map imageData = cloudinaryService.uploadImage(request.getImageUrl(), "ecommerce-be/category");
        Map iconData = cloudinaryService.uploadImage(request.getIconUrl(), "ecommerce-be/category");

        Category category = categoryMapper.toCategory(request);

        category.setSlug(generateSlug(category.getName()));
        category.setImageUrl((String) imageData.get("url"));
        category.setIconUrl((String) iconData.get("url"));

        if (request.getParentId() != null) {
            Optional<Category> parentCategory = categoryRepository.findById(request.getParentId());
            if (parentCategory.isPresent()) {
                category.setParent(parentCategory.get());
            } else {
                throw new AppException(ErrorCode.PARENT_CATEGORY_NOT_FOUND);
            }
        } else {
            category.setParent(null);
        }

        try {
            log.info(
                    "Category created with createdAt: {} and lastUpdatedAt: {}",
                    category.getCreatedAt(),
                    category.getLastUpdatedAt());
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryCreationRequest request) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByName(request.getName())
                && !category.getName().equals(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_DUPLICATE);
        }

        category.setName(request.getName());
        category.setSlug(generateSlug(request.getName()));
        category.setDescription(request.getDescription());

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            Map imageData = cloudinaryService.uploadImage(request.getImageUrl(), "ecommerce-be/category");
            category.setImageUrl((String) imageData.get("url"));
        }

        if (request.getIconUrl() != null && !request.getIconUrl().isEmpty()) {
            Map iconData = cloudinaryService.uploadImage(request.getIconUrl(), "ecommerce-be/category");
            category.setIconUrl((String) iconData.get("url"));
        }
        if (request.getParentId() != null) {
            Category parentCategory = categoryRepository
                    .findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARENT_CATEGORY_NOT_FOUND));
            category.setParent(parentCategory);
        } else {
            category.setParent(null);
        }

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    public CategoryResponse getOneCategory(Long id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(categoryMapper::toCategoryResponse).collect(Collectors.toList());
    }

    public String generateSlug(String name) {
        String noAccents = name.replaceAll("[áàạảãâấầậẩẫăắằặẳẵ]", "a")
                .replaceAll("[éèẹẻẽêếềệểễ]", "e")
                .replaceAll("[íìịỉĩ]", "i")
                .replaceAll("[óòọỏõôốồộổỗơớờợởỡ]", "o")
                .replaceAll("[úùụủũưứừựửữ]", "u")
                .replaceAll("[ýỳỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[ÁÀẠẢÃÂẤẦẬẨẪĂẮẰẶẲẴ]", "A")
                .replaceAll("[ÉÈẸẺẼÊẾỀỆỂỄ]", "E")
                .replaceAll("[ÍÌỊỈĨ]", "I")
                .replaceAll("[ÓÒỌỎÕÔỐỒỘỔỖƠỚỜỢỞỠ]", "O")
                .replaceAll("[ÚÙỤỦŨƯỨỪỰỬỮ]", "U")
                .replaceAll("[ÝỲỴỶỸ]", "Y")
                .replaceAll("[Đ]", "D");
        String sanitized = noAccents.replaceAll("[^a-zA-Z0-9\\s]", "");
        String baseSlug = sanitized.trim().toLowerCase().replaceAll("\\s+", "-");

        String randomCode = UUID.randomUUID().toString().substring(0, 8);
        return baseSlug + "-" + randomCode;
    }
}
