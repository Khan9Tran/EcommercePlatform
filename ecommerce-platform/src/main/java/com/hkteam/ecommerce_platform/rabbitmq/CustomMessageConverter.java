package com.hkteam.ecommerce_platform.rabbitmq;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkteam.ecommerce_platform.dto.request.*;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class CustomMessageConverter implements MessageConverter {

    ObjectMapper objectMapper;

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        byte[] bytes = null;
        try {
            bytes = objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (object instanceof EmailMessageRequest) {
            messageProperties.setHeader("messageType", "email");
        } else if (object instanceof ImageMessageRequest) {
            messageProperties.setHeader("messageType", "image");
        } else if (object instanceof DeleteImageRequest) {
            messageProperties.setHeader("messageType", "deleteImage");
        } else if (object instanceof UpdateCategoryEsProductRequest) {
            messageProperties.setHeader("messageType", "updateCategoryEsProduct");
        } else if (object instanceof UpdateBrandEsProductRequest) {
            messageProperties.setHeader("messageType", "updateBrandEsProduct");
        } else {
            throw new MessageConversionException("Unknown message type");
        }

        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            String messageType = message.getMessageProperties().getHeader("messageType");
            if ("email".equals(messageType)) {
                return objectMapper.readValue(message.getBody(), EmailMessageRequest.class);
            } else if ("image".equals(messageType)) {
                return objectMapper.readValue(message.getBody(), ImageMessageRequest.class);
            } else if ("deleteImage".equals(messageType)) {
                return objectMapper.readValue(message.getBody(), DeleteImageRequest.class);
            } else if ("updateCategoryEsProduct".equals(messageType)) {
                return objectMapper.readValue(message.getBody(), UpdateCategoryEsProductRequest.class);
            } else if ("updateBrandEsProduct".equals(messageType)) {
                return objectMapper.readValue(message.getBody(), UpdateBrandEsProductRequest.class);

            } else {
                throw new MessageConversionException("Unknown message type");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
