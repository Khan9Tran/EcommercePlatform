package com.hkteam.ecommerce_platform.rabbitmq;

import com.hkteam.ecommerce_platform.dto.request.ImageMessageRequest;
import com.hkteam.ecommerce_platform.service.CloudinaryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageUploadConsumer {
    CloudinaryService cloudinaryService;

    @RabbitListener(queues = RabbitMQConfig.IMAGE_QUEUE)
    public void receiveFileUploadMessage(ImageMessageRequest message) {
        try {
            cloudinaryService.uploadImage(message.getImage(), message.getType().name().toLowerCase());

        } catch (Exception e) {
            log.error("Error uploading image {}", e.getMessage());
        }
    }
}
