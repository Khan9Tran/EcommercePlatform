package com.hkteam.ecommerce_platform.service;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.response.VideoResponse;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.enums.TypeImage;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.VideoUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VideoService {
    CloudinaryService cloudinaryService;
    ProductRepository productRepository;
    AuthenticatedUserUtil authenticatedUserUtil;

    @PreAuthorize("hasRole('SELLER')")
    public VideoResponse uploadVideoProduct(String productId, MultipartFile videoFile) {
        VideoUtils.validateVideo(videoFile);

        var user = authenticatedUserUtil.getAuthenticatedUser();

        VideoResponse videoResponse;

        Product product =
                productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        var store = product.getStore();

        if (store == null) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }

        if (!store.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        try {
            Map<String, Object> uploadResult = cloudinaryService.uploadVideo(
                    videoFile, TypeImage.MAIN_VIDEO_OF_PRODUCT.name().toLowerCase());

            if (uploadResult.get("url") == null) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            } else {
                if (product.getVideoUrl() != null) {
                    cloudinaryService.deleteVideo(product.getVideoUrl());
                }

                product.setVideoUrl(uploadResult.get("url").toString());
                try {
                    productRepository.save(product);
                } catch (DataIntegrityViolationException e) {
                    log.info("Error while saving at upload product video: {}", e.getMessage());
                    throw new AppException(ErrorCode.UNKNOWN_ERROR);
                }

                videoResponse = VideoResponse.builder()
                        .productId(productId)
                        .videoUrl(uploadResult.get("secure_url").toString())
                        .format(uploadResult.get("format").toString())
                        .publicId(uploadResult.get("public_id").toString())
                        .size((int) uploadResult.get("bytes"))
                        .createdAt(uploadResult.get("created_at").toString())
                        .build();
            }
            return videoResponse;
        } catch (Exception e) {
            log.info("Error while uploading at upload product video: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    public void deleteProductVideo(Long productId) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        Product product =
                productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        var store = product.getStore();

        if (store == null) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }

        if (!product.getStore().getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (product.getVideoUrl() == null) {
            throw new AppException(ErrorCode.FILE_NULL);
        }

        try {
            cloudinaryService.deleteVideo(product.getVideoUrl());

            product.setVideoUrl(null);

            try {
                productRepository.save(product);
            } catch (DataIntegrityViolationException e) {
                log.info("Error while saving at delete product video: {}", e.getMessage());
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }
        } catch (Exception e) {
            log.info("Error while deleting at delete product video: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }
}
