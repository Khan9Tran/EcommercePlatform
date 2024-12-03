package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ReviewResponse;
import com.hkteam.ecommerce_platform.entity.useractions.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toReview(ReviewCreationRequest request);

    ReviewResponse toReviewResponse(Review review);
}
