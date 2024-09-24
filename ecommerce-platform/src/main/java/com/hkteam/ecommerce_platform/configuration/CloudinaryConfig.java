package com.hkteam.ecommerce_platform.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

import lombok.experimental.FieldDefaults;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Configuration
public class CloudinaryConfig {
    static final String CLOUD_NAME = "cloud_name";
    static final String API_KEY = "api_key";
    static final String API_SECRET = "api_secret";

    @Value("${cloudinary.name}")
    String cloudName;

    @Value("${cloudinary.api-key}")
    String apiKey;

    @Value("${cloudinary.api-secret}")
    String apiSecret;

    @Bean
    public Cloudinary getCloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put(CLOUD_NAME, cloudName);
        config.put(API_KEY, apiKey);
        config.put(API_SECRET, apiSecret);
        return new Cloudinary(config);
    }
}
