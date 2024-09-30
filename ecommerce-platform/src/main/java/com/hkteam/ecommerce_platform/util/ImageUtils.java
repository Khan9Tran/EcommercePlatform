package com.hkteam.ecommerce_platform.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ImageUtils {
    private static final long MAX_FILE_SIZE = 1000L * 1024;
    private static final List<String> ACCEPTED_FILE_TYPES = Arrays.asList("jpg", "png", "jpeg", "gif");

    public static void validateImage(MultipartFile image) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_LIMIT_OF_1MB);
        }

        String extension = getFileExtension(image.getOriginalFilename());
        if (!ACCEPTED_FILE_TYPES.contains(extension)) {
            throw new AppException(ErrorCode.ACCEPTED_FILE_TYPES);
        }

        if (image.isEmpty()) {
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
