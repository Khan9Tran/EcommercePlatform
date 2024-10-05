package com.hkteam.ecommerce_platform.dto.request;

import com.hkteam.ecommerce_platform.enums.TypeImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ImageMessageRequest {
    MultipartFile image;
    TypeImage type;
}
