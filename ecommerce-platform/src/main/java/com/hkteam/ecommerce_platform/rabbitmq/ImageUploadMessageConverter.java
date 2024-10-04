package com.hkteam.ecommerce_platform.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkteam.ecommerce_platform.dto.request.EmailMessageRequest;
import com.hkteam.ecommerce_platform.dto.request.ImageMessageRequest;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class ImageUploadMessageConverter implements MessageConverter {
    ObjectMapper objectMapper;

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        byte[] bytes = null;
        try {
            bytes = objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            return objectMapper.readValue(message.getBody(), ImageMessageRequest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
