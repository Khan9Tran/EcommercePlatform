package com.hkteam.ecommerce_platform.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateValueRequest {
    @NonNull
    Long id;

    @NonNull
    String value;
}
