package com.hkteam.ecommerce_platform.service;

import java.io.IOException;
import java.util.*;

import com.hkteam.ecommerce_platform.dto.request.EmailMessageRequest;
import com.hkteam.ecommerce_platform.dto.request.ImageMessageRequest;
import com.hkteam.ecommerce_platform.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.constant.ImageAttribute;
import com.hkteam.ecommerce_platform.dto.request.DeleteProductImageRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductImageUploadRequest;
import com.hkteam.ecommerce_platform.dto.response.ImageResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductImageResponse;
import com.hkteam.ecommerce_platform.entity.category.Category;
import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import com.hkteam.ecommerce_platform.entity.product.Brand;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.enums.TypeImage;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
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
    private final UserRepository userRepository;
    CloudinaryService cloudinaryService;
    CategoryRepository categoryRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    BrandRepository brandRepository;
    ProductRepository productRepository;
    ProductImageRepository productImageRepository;

    RabbitTemplate rabbitTemplate;

    @PreAuthorize("hasRole('ADMIN')")
    public ImageResponse uploadCategoryImage(MultipartFile image, Long categoryId) {
        ImageUtils.validateImage(image);

        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        ImageResponse imageResponse;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> img = (cloudinaryService.uploadImage(
                    image, TypeImage.MAIN_IMAGE_OF_CATEGORY.name().toLowerCase()));

            if (img.get("url") == null) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            } else {
                if (category.getImageUrl() != null) {
                    cloudinaryService.deleteImage(category.getImageUrl());
                }

                category.setImageUrl(img.get("url").toString());
                saveCategoryUpload(category);

                imageResponse = ImageResponse.builder()
                        .format(img.get(ImageAttribute.FORMAT).toString())
                        .secureUrl(img.get(ImageAttribute.SECURE_URL).toString())
                        .createdAt(img.get(ImageAttribute.CREATED_AT).toString())
                        .url(img.get(ImageAttribute.URL).toString())
                        .bytes((int) img.get(ImageAttribute.BYTES))
                        .width((int) img.get(ImageAttribute.WIDTH))
                        .height((int) img.get(ImageAttribute.HEIGHT))
                        .build();
            }

            return imageResponse;
        } catch (Exception e) {
            log.info("Error while uploading at upload category image: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ImageResponse uploadCategoryIcon(MultipartFile image, Long categoryId) {
        ImageUtils.validateImage(image);

        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        ImageResponse imageResponse;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> img = (cloudinaryService.uploadImage(
                    image, TypeImage.CATEGORY_ICON.name().toLowerCase()));

            if (img.get("url") == null) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            } else {
                if (category.getIconUrl() != null) {
                    cloudinaryService.deleteImage(category.getIconUrl());
                }

                category.setIconUrl(img.get("url").toString());
                saveCategoryUpload(category);

                imageResponse = ImageResponse.builder()
                        .format(img.get(ImageAttribute.FORMAT).toString())
                        .secureUrl(img.get(ImageAttribute.SECURE_URL).toString())
                        .createdAt(img.get(ImageAttribute.CREATED_AT).toString())
                        .url(img.get(ImageAttribute.URL).toString())
                        .bytes((int) img.get(ImageAttribute.BYTES))
                        .width((int) img.get(ImageAttribute.WIDTH))
                        .height((int) img.get(ImageAttribute.HEIGHT))
                        .build();
            }

            return imageResponse;
        } catch (Exception e) {
            log.info("Error while uploading at upload category icon: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    private void saveCategoryUpload(Category category) {
        try {
            categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while saving at upload category image");
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategoryImage(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getImageUrl() == null) {
            throw new AppException(ErrorCode.FILE_NULL);
        }

        try {
            cloudinaryService.deleteImage(category.getImageUrl());

            category.setImageUrl(null);

            saveCategoryDelete(category);
        } catch (Exception e) {
            log.info("Error while deleting at delete category image: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategoryIcon(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (category.getIconUrl() == null) {
            throw new AppException(ErrorCode.FILE_NULL);
        }

        try {
            cloudinaryService.deleteImage(category.getIconUrl());

            category.setIconUrl(null);

            saveCategoryDelete(category);
        } catch (Exception e) {
            log.info("Error while deleting at delete category icon: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    private void saveCategoryDelete(Category category) {
        try {
            categoryRepository.save(category);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while saving at delete category image: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    public ImageResponse uploadUserImage(MultipartFile image) {
        ImageUtils.validateImage(image);

        ImageResponse imageResponse;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> img = (cloudinaryService.uploadImage(
                    image, TypeImage.MAIN_IMAGE_OF_USER.name().toLowerCase()));

            if (img.get("url") == null) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            } else {
                var user = authenticatedUserUtil.getAuthenticatedUser();

                if (user.getImageUrl() != null) {
                    cloudinaryService.deleteImage(user.getImageUrl());
                }

                user.setImageUrl(img.get("url").toString());
                try {
                    userRepository.save(user);
                } catch (DataIntegrityViolationException e) {
                    log.info("Error while saving at upload user image");
                    throw new AppException(ErrorCode.UNKNOWN_ERROR);
                }

                imageResponse = ImageResponse.builder()
                        .format(img.get(ImageAttribute.FORMAT).toString())
                        .secureUrl(img.get(ImageAttribute.SECURE_URL).toString())
                        .createdAt(img.get(ImageAttribute.CREATED_AT).toString())
                        .url(img.get(ImageAttribute.URL).toString())
                        .bytes((int) img.get(ImageAttribute.BYTES))
                        .width((int) img.get(ImageAttribute.WIDTH))
                        .height((int) img.get(ImageAttribute.HEIGHT))
                        .build();
            }

            return imageResponse;
        } catch (Exception e) {
            log.error("Error while uploading at upload user image: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    public void deleteUserImage() {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        if (user.getImageUrl() == null) {
            throw new AppException(ErrorCode.FILE_NULL);
        }

        try {
            cloudinaryService.deleteImage(user.getImageUrl());

            user.setImageUrl(null);

            try {
                userRepository.save(user);
            } catch (DataIntegrityViolationException e) {
                log.info("Error while saving at delete user image: {}", e.getMessage());
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }
        } catch (Exception e) {
            log.info("Error while deleting at delete user image: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ImageResponse uploadBrandLogo(MultipartFile logoUrl, Long brandId) {
        ImageUtils.validateImage(logoUrl);

        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        ImageResponse imageResponse;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> img = (cloudinaryService.uploadImage(
                    logoUrl, TypeImage.MAIN_LOGO_OF_BRAND.name().toLowerCase()));

            if (img.get("url") == null) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            } else {
                if (brand.getLogoUrl() != null) {
                    cloudinaryService.deleteImage(brand.getLogoUrl());
                }

                brand.setLogoUrl(img.get("url").toString());
                try {
                    brandRepository.save(brand);
                } catch (DataIntegrityViolationException e) {
                    log.info("Error while saving at upload brand logo: {}", e.getMessage());
                    throw new AppException(ErrorCode.UNKNOWN_ERROR);
                }

                imageResponse = ImageResponse.builder()
                        .format(img.get(ImageAttribute.FORMAT).toString())
                        .secureUrl(img.get(ImageAttribute.SECURE_URL).toString())
                        .createdAt(img.get(ImageAttribute.CREATED_AT).toString())
                        .url(img.get(ImageAttribute.URL).toString())
                        .bytes((int) img.get(ImageAttribute.BYTES))
                        .width((int) img.get(ImageAttribute.WIDTH))
                        .height((int) img.get(ImageAttribute.HEIGHT))
                        .build();
            }

            return imageResponse;
        } catch (Exception e) {
            log.info("Error while uploading at upload brand logo: {}", e.getMessage());
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrandLogo(Long brandId) {
        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        if (brand.getLogoUrl() == null) {
            throw new AppException(ErrorCode.FILE_NULL);
        }

        try {
            cloudinaryService.deleteImage(brand.getLogoUrl());

            brand.setLogoUrl(null);

            try {
                brandRepository.save(brand);
            } catch (DataIntegrityViolationException e) {
                log.info("Error while saving at delete brand logo: {}", e.getMessage());
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }
        } catch (Exception e) {
            log.info("Error while deleting at delete brand logo: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    public ImageResponse uploadProductMainImage(MultipartFile image, String productId) {
        ImageUtils.validateImage(image);
        try {
            var product =
                    productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            if (!authenticatedUserUtil.isOwner(product)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            addImageToQueue(productId, TypeImage.MAIN_IMAGE_OF_PRODUCT, convertImageToByteArray(image));
        }
        catch (Exception e)
        {
            log.error("Error when upload image: " + e.getMessage());
            throw  new AppException(ErrorCode.UNKNOWN_ERROR);
        }
        return ImageResponse.builder().url("https://res.cloudinary.com/dftaajyn6/image/upload/v1730254100/tmp/wdyphd3wjf1wzj7vit4m.webp").build();
//
    }

    @PreAuthorize("hasRole('SELLER')")
    public void deleteProductMainImage(String productId) {
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

        if (product.getMainImageUrl() == null) {
            throw new AppException(ErrorCode.FILE_NULL);
        }

        try {
            cloudinaryService.deleteImage(product.getMainImageUrl());

            product.setMainImageUrl(null);

            try {
                productRepository.save(product);
            } catch (DataIntegrityViolationException e) {
                log.info("Error while saving at delete product main image url: {}", e.getMessage());
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }
        } catch (Exception e) {
            log.info("Error while deleting at delete product main image url: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    public ProductImageResponse uploadProductListImage(ProductImageUploadRequest request, String productId) {
        request.getImages().forEach(ImageUtils::validateImage);


        Product product =
                productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!authenticatedUserUtil.isOwner(product)) throw  new AppException(ErrorCode.UNAUTHORIZED);

        try {
            addImageToQueue(productId, TypeImage.LIST_IMAGE_PRODUCT, convertImagesToByteArray(request.getImages()));
        }
        catch (Exception e) {
            log.error("Error when upload list image: " + e.getMessage());
            throw  new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        List<ImageResponse> imageResponses = new ArrayList<>();
        int size = request.getImages().size();

        for (int i = 0; i< size; i++) {
            var imageResponse = ImageResponse.builder()
                            .url("https://res.cloudinary.com/dftaajyn6/image/upload/v1730254100/tmp/wdyphd3wjf1wzj7vit4m.webp")
                            .build();
            imageResponses.add(imageResponse);
        }

        return ProductImageResponse.builder().images(imageResponses).build();
    }

    @PreAuthorize("hasRole('SELLER')")
    public void deleteProductListImage(String productId, DeleteProductImageRequest request) {
        var product =
                productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!authenticatedUserUtil.isOwner(product)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        var imageIds = request.getListImageIds();
        if (imageIds == null || imageIds.isEmpty()) {
            throw new AppException(ErrorCode.LIST_PRODUCT_IMAGE_NOT_BLANK);
        }


        Set<Long> uniqueImageIds = new HashSet<>(imageIds);
        if (uniqueImageIds.size() < imageIds.size()) {
            throw new AppException(ErrorCode.DUPLICATE_PRODUCT_IMAGE_IDS);
        }

        List<ProductImage> productImages = productImageRepository.findAllById(uniqueImageIds);

        if (productImages.size() != uniqueImageIds.size()) {
            throw new AppException(ErrorCode.LIST_PRODUCT_IMAGE_NOT_FOUND);
        }

        for (ProductImage image : productImages) {
            if (!image.getProduct().getId().equals(product.getId())) {
                throw new AppException(ErrorCode.IMAGE_DOES_NOT_BELONG_TO_PRODUCT);
            }
        }

        productImages.forEach(image -> {
            try {
                cloudinaryService.deleteImage(image.getUrl());
            } catch (IOException e) {
                throw new AppException(ErrorCode.DELETE_FILE_FAILED);
            }
            productImageRepository.delete(image);

        });
    }


    private void addImageToQueue(String id, TypeImage type, byte[] image) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IMAGE_QUEUE,
                ImageMessageRequest.builder()
                        .id(id)
                        .type(type)
                        .image(List.of(image))
                        .build());

        log.info("Image sent to the queue for processing: {}", id);
    }

    private void addImageToQueue(String id, TypeImage type, List<byte[]> images) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.IMAGE_QUEUE,
                ImageMessageRequest.builder()
                        .id(id)
                        .type(type)
                        .image(images)
                        .build());

        log.info("Image sent to the queue for processing: {}", id);
    }

    private byte[] convertImageToByteArray(MultipartFile file) throws IOException {
        return file.getBytes();
    }

    private List<byte[]> convertImagesToByteArray(List<MultipartFile> files) throws IOException {
        List<byte[]> byteArrayList = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] bytes = file.getBytes();
            byteArrayList.add(bytes);
        }
        return byteArrayList;
    }




}
