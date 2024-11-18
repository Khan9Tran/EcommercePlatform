package com.hkteam.ecommerce_platform.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "emailQueue";
    public static final String IMAGE_QUEUE = "imageQueue";
    public static final String DELETE_IMAGE_QUEUE = "deleteImageQueue";
    public static final String CATE_ES_PRODUCT_QUEUE = "updateCategoryEsProductQueue";
    public static final String BRAND_ES_PRODUCT_QUEUE = "updateBrandEsProductQueue";

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public Queue imageQueue() {
        return new Queue(IMAGE_QUEUE, true);
    }

    @Bean
    public Queue deleteImageQueue() {
        return new Queue(DELETE_IMAGE_QUEUE, true);
    }

    @Bean
    public Queue updateCategoryEsProductQueue() {
        return new Queue(CATE_ES_PRODUCT_QUEUE, true);
    }

    @Bean
    public Queue updateBrandEsProductQueue() {
        return new Queue(BRAND_ES_PRODUCT_QUEUE, true);
    }


    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory, CustomMessageConverter customMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(customMessageConverter);
        return rabbitTemplate;
    }
}
