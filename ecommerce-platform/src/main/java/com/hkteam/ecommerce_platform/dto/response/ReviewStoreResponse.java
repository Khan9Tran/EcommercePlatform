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
public class ReviewStoreResponse {
    String productMainImageUrl;
    String productName;
    List<ReviewListValueStoreResponse> productValues;
    String productSlug;
    String userName;
    String userAccount;
    String userAvatar;
    float reviewRating;
    String reviewComment;
    String reviewVideo;
    List<ReviewListImageResponse> reviewImages;
    Instant lastUpdatedAt;
}
