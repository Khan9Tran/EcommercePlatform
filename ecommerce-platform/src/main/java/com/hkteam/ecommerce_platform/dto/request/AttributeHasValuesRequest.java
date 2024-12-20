package com.hkteam.ecommerce_platform.dto.request;

import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
