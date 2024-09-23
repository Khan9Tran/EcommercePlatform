package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CategoryCreationRequest {
    @NotBlank(message = "NOT_BLANK")
    String name;

    String description;
    MultipartFile imageUrl;
    MultipartFile iconUrl;
    Long parentId;
}
