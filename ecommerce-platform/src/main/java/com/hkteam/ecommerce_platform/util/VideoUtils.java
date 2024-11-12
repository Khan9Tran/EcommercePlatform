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
public class VideoUtils {
    long MAX_VIDEO_SIZE = 100L * 1024 * 1024;
    List<String> ACCEPTED_VIDEO_TYPES = Arrays.asList("mp4", "avi", "mov", "mkv");

    public void validateVideo(MultipartFile video) {
        if (video.getSize() > MAX_VIDEO_SIZE) {
            throw new AppException(ErrorCode.FILE_LIMIT_OF_50MB);
        }

        String extension = getFileExtension(video.getOriginalFilename());
        if (!ACCEPTED_VIDEO_TYPES.contains(extension)) {
            throw new AppException(ErrorCode.ACCEPTED_VIDEO_TYPES);
        }

        if (video.isEmpty()) {
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
