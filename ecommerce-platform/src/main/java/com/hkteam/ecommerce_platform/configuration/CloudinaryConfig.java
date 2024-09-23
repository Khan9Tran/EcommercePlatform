package com.hkteam.ecommerce_platform.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary getCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", "dftaajyn6");
        config.put("api_key", "299729385568347");
        config.put("api_secret", "apqi4hlLg2MZJZZm1B2v5voXx7Y");
        return new Cloudinary(config);
    }
}
