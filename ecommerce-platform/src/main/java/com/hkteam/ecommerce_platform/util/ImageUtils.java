package com.hkteam.ecommerce_platform.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageUtils {
    long MAX_FILE_SIZE = 1000L * 1024;
    List<String> ACCEPTED_IMAGE_TYPES = Arrays.asList("jpg", "png", "jpeg");

    public void validateImage(MultipartFile image) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_LIMIT_OF_1MB);
        }

        String extension = getFileExtension(image.getOriginalFilename());
        if (!ACCEPTED_IMAGE_TYPES.contains(extension)) {
            throw new AppException(ErrorCode.ACCEPTED_IMAGE_TYPES);
        }

        if (image.isEmpty()) {
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new AppException(ErrorCode.FILE_NULL);
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
