package com.hkteam.ecommerce_platform.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CateHasComponentResponse {
    String name;
    Long id;
    List<ComponentDetailResponse> components;
}
