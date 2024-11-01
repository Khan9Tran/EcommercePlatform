package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CategoryCreationRequest {
    @NotBlank(message = "NOT_BLANK")
    @NonNull
    String name;

    @NotNull
    String description;

    Long parentId;
}
