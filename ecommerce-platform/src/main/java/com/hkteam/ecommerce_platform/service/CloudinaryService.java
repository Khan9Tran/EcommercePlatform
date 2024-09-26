package com.hkteam.ecommerce_platform.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CloudinaryService {
    Cloudinary cloudinary;

    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = Map.of("folder", folder);

            @SuppressWarnings("unchecked")
            Map<String, Object> upload = cloudinary.uploader().upload(file.getBytes(), options);
            return upload;
        } catch (IOException io) {
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    public void deleteImage(String imageUrl) throws IOException {
        log.info("Attempting to delete image with URL: {}", imageUrl);
        String publicId = extractPublicId(imageUrl);
        log.info("Extracted publicId: {}", publicId);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().destroy(publicId, Map.of());
        log.info("Cloudinary response after deleting image: {}", result);

        if ("ok".equals(result.get("result"))) {
            log.info("Image successfully deleted: {}", publicId);
        } else {
            log.warn("Failed to delete image with publicId: {}", publicId);
        }
    }

    private String extractPublicId(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 2] + "/" + parts[parts.length - 1].split("\\.")[0];
    }
}
