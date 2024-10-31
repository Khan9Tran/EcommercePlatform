package com.hkteam.ecommerce_platform.rabbitmq;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.DeleteImageRequest;
import com.hkteam.ecommerce_platform.dto.request.ImageMessageRequest;
import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import com.hkteam.ecommerce_platform.enums.TypeImage;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.ProductImageRepository;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.service.CloudinaryService;
import com.hkteam.ecommerce_platform.service.ImageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageUploadConsumer {
    CloudinaryService cloudinaryService;
    ImageService imageService;
    ProductRepository productRepository;
    ProductImageRepository productImageRepository;

    @RabbitListener(queues = RabbitMQConfig.IMAGE_QUEUE)
    public void receiveFileUploadMessage(ImageMessageRequest message) {
        try {
            uploadImageToCloudinary(message);
        } catch (Exception e) {
            log.error("Error uploading image {}", e.getMessage());
        }
    }

    private void uploadImageToCloudinary(ImageMessageRequest request) throws IOException {
        var product = productRepository
                .findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getType().equals(TypeImage.MAIN_IMAGE_OF_PRODUCT)) {
            Map<String, Object> img = cloudinaryService.uploadImage(
                    request.getImage().get(0),
                    TypeImage.MAIN_IMAGE_OF_PRODUCT.toString().toLowerCase());

            if (!Objects.isNull(product.getMainImageUrl())) {
                cloudinaryService.deleteImage(product.getMainImageUrl());
            }

            product.setMainImageUrl(getUrl(img));

            try {
                productRepository.save(product);
            } catch (DataIntegrityViolationException exception) {
                log.error("Error when up load main image for product " + product.getId());
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }
            log.info("Complate upload image for product: " + product.getId());

        } else if (request.getType().equals(TypeImage.LIST_IMAGE_PRODUCT)) {
            for (byte[] image : request.getImage()) {
                try {
                    Map<String, Object> img = cloudinaryService.uploadImage(
                            image, TypeImage.LIST_IMAGE_PRODUCT.name().toLowerCase());
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .url(getUrl(img))
                            .build();
                    productImageRepository.save(productImage);

                } catch (Exception e) {
                    log.info("Error while uploading image for product ID {}: {}", request.getId(), e.getMessage());
                    throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
                }
            }
        }
    }

    private String getUrl(Map<String, Object> img) {
        if (Objects.isNull(img.get("url"))) {
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
        return img.get("url").toString();
    }

    @RabbitListener(queues = RabbitMQConfig.DELETE_IMAGE_QUEUE)
    public void receiveDeleteImageMessage(DeleteImageRequest message) {
        try {
            deleteImage(message);
        } catch (Exception e) {
            log.error("Error deleting image: of " + message.getTypeImage() + " with id " + message.getId() + ":"
                    + e.getMessage());
        }
    }

    private void deleteImage(DeleteImageRequest request) {
        request.getUrl().forEach(url -> {
            try {
                cloudinaryService.deleteImage(url);
            } catch (IOException e) {
                log.error("Error deleting image: {}", e.getMessage());
                throw new AppException(ErrorCode.DELETE_FILE_FAILED);
            }
        });
    }
}
