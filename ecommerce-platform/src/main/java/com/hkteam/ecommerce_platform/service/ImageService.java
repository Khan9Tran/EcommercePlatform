package com.hkteam.ecommerce_platform.service;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageService {
    public ApiResponse<Object> uploadCategoryImage(MultipartFile image) {

    }
}
