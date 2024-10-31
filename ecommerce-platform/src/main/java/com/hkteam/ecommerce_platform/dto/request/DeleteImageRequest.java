package com.hkteam.ecommerce_platform.dto.request;

import java.util.List;

import com.hkteam.ecommerce_platform.enums.TypeImage;

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
public class DeleteImageRequest {
    List<String> url;
    String id;
    TypeImage typeImage;
}
