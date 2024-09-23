package com.hkteam.ecommerce_platform.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryService {
    private Cloudinary cloudinary;

    public Map uploadImage(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = Map.of("folder", folder);
            Map data = this.cloudinary.uploader().upload(file.getBytes(), options);
            return data;
        } catch (IOException io) {
            throw new RuntimeException("Image upload fail");
        }
    }
}
