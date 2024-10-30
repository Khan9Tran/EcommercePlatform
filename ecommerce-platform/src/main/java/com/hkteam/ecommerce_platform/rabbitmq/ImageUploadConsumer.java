package com.hkteam.ecommerce_platform.rabbitmq;

import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.enums.TypeImage;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.ProductImageRepository;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.service.ImageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ImageMessageRequest;
import com.hkteam.ecommerce_platform.service.CloudinaryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.rmi.AccessException;
import java.util.Map;
import java.util.Objects;

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

    private  void  uploadImageToCloudinary(ImageMessageRequest request) throws IOException {
        var product = productRepository.findById(request.getId()).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getType().equals(TypeImage.MAIN_IMAGE_OF_PRODUCT)) {
            Map<String, Object> img = cloudinaryService.uploadImage(request.getImage().get(0), TypeImage.MAIN_IMAGE_OF_PRODUCT.toString().toLowerCase());

            if (!Objects.isNull(product.getMainImageUrl())) {
                cloudinaryService.deleteImage(product.getMainImageUrl());
            }

            product.setMainImageUrl(getUrl(img));

            try {
                productRepository.save(product);
            }
            catch (DataIntegrityViolationException exception) {
                log.error("Error when up load main image for product " + product.getId());
                throw  new AppException(ErrorCode.UNKNOWN_ERROR);
            }
            log.info("Complate upload image for product: " + product.getId());


        }
        else if (request.getType().equals(TypeImage.LIST_IMAGE_PRODUCT)) {
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
}
