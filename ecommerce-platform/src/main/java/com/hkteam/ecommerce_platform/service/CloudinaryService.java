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

    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = Map.of("folder", folder);
            Map data = this.cloudinary.uploader().upload(file.getBytes(), options);
            return data;
        } catch (IOException io) {
            throw new RuntimeException("Image upload fail");
        }
    }

    public void deleteImage(String imageUrl) throws IOException {
        log.info("Attempting to delete image with URL: {}", imageUrl);
        String publicId = extractPublicId(imageUrl);
        log.info("Extracted publicId: {}", publicId);

        Map result = cloudinary.uploader().destroy(publicId, Map.of());
        log.info("Cloudinary response after deleting image: {}", result);

        if ("ok".equals(result.get("result"))) {
            log.info("Image successfully deleted: {}", publicId);
        } else {
            log.warn("Failed to delete image with publicId: {}", publicId);
        }
    }

    private String extractPublicId(String imageUrl) {
        String[] parts = imageUrl.split("/");
        String folderPath = parts[parts.length - 3] + "/" + parts[parts.length - 2];
        String publicIdWithExtension = parts[parts.length - 1];
        return folderPath + "/" + publicIdWithExtension.split("\\.")[0];
    }
}
