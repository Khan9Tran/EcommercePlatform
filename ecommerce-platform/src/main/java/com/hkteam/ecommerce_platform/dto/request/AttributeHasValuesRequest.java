package com.hkteam.ecommerce_platform.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AttributeHasValuesRequest {
    @NonNull
    String name;

    @NonNull
    List<String> value;
}
