package com.hkteam.ecommerce_platform.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.UpdateBrandEsProductRequest;
import com.hkteam.ecommerce_platform.dto.request.UpdateCategoryEsProductRequest;
import com.hkteam.ecommerce_platform.repository.ProductElasticsearchRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UpdateProductElasticSearchConsumer {
    ProductElasticsearchRepository productElasticsearchRepository;

    @RabbitListener(queues = RabbitMQConfig.CATE_ES_PRODUCT_QUEUE)
    public void receiveCategoryUpdateRequest(UpdateCategoryEsProductRequest message) {
        log.info("Receive message update category for product: {}", message.getId());
        try {
            var products = productElasticsearchRepository.findByCategoryId(message.getId());
            if (products.isEmpty()) {
                log.info("No product need update: {}", message.getId());
                return;
            }

            if (message.getIsDeleted()) {
                products.forEach(product -> {
                    product.setCategoryId(null);
                    product.setCategoryName(null);
                });
            } else {
                products.forEach(product -> {
                    product.setCategoryId(message.getId());
                    product.setCategoryName(message.getName());
                });
            }

            productElasticsearchRepository.saveAll(products);

        } catch (Exception e) {
            log.error("Error when update category for product in es: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.BRAND_ES_PRODUCT_QUEUE)
    public void receiveBrandUpdateRequest(UpdateBrandEsProductRequest message) {
        log.info("Receive message update brand for product: {}", message.getId());
        try {
            var products = productElasticsearchRepository.findByCategoryId(message.getId());
            if (products.isEmpty()) {
                log.info("No product need update: {}", message.getId());
                return;
            }

            if (message.getIsDeleted()) {
                products.forEach(product -> {
                    product.setBrandId(null);
                    product.setBrandName(null);
                });
            } else {
                products.forEach(product -> {
                    product.setBrandId(message.getId());
                    product.setBrandName(message.getName());
                });
            }

            productElasticsearchRepository.saveAll(products);

        } catch (Exception e) {
            log.error("Error when update brand for product in es: {}", e.getMessage());
        }
    }
}
