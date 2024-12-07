package com.hkteam.ecommerce_platform.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ReviewListOneProductResponse {
    Long id;
    float rating;
    String comment;
    String videoUrl;
    Instant lastUpdatedAt;
    List<ReviewImageListOneProductResponse> images;
    ReviewProductUserResponse user;
}