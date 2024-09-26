package com.hkteam.ecommerce_platform.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ImageService {
    public ApiResponse<Object> uploadCategoryImage(MultipartFile image) {}
}
