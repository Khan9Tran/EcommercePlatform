package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
public class ReviewCreationRequest {
    @DecimalMin(value = "0.0", message = "RATING_INVALID")
    @DecimalMax(value = "5.0", message = "RATING_INVALID")
    float rating;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 1, max = 255, message = "COMMENT_INVALID")
    String comment;

    String orderId;
}
