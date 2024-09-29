package com.hkteam.ecommerce_platform.service;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.response.ImageResponse;
import com.hkteam.ecommerce_platform.entity.category.Category;
import com.hkteam.ecommerce_platform.enums.TypeImage;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.CategoryRepository;
import com.hkteam.ecommerce_platform.util.ImageUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageService {
    CloudinaryService cloudinaryService;
    CategoryRepository categoryRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ImageResponse uploadCategoryImage(MultipartFile image, Long categoryId) {
        ImageUtils.validateImage(image);

        ImageResponse imageResponse;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> img = (cloudinaryService.uploadImage(
                    image, TypeImage.MAIN_IMAGE_OF_CATEGORY.name().toLowerCase()));

            if (img.get("url") == null) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            } else {
                Category category = categoryRepository
                        .findById(categoryId)
                        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

                if (category.getImageUrl() != null) {
                    cloudinaryService.deleteImage(category.getImageUrl());
                }

                category.setImageUrl(img.get("url").toString());
                categoryRepository.save(category);

                imageResponse = ImageResponse.builder()
                        .format(img.get("format").toString())
                        .secureUrl(img.get("secure_url").toString())
                        .createdAt(img.get("created_at").toString())
                        .url(img.get("url").toString())
                        .bytes((int) img.get("bytes"))
                        .width((int) img.get("width"))
                        .height((int) img.get("height"))
                        .build();
            }

            return imageResponse;
        } catch (Exception e) {
            log.error("Error while uploading image: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategoryImage(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        try {
            if (category.getImageUrl() != null) {
                cloudinaryService.deleteImage(category.getImageUrl());

                category.setImageUrl(null);
                categoryRepository.save(category);
            } else {
                throw new AppException(ErrorCode.IMAGE_NULL);
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.IMAGE_NULL);
        }
    }
}
