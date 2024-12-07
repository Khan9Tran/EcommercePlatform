package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ReviewUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ReviewListOneProductResponse;
import com.hkteam.ecommerce_platform.dto.response.ReviewProductUserResponse;
import com.hkteam.ecommerce_platform.dto.response.ReviewResponse;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.entity.useractions.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toReview(ReviewCreationRequest request);

    ReviewResponse toReviewResponse(Review review);

    ReviewListOneProductResponse toReviewListOneProductResponse(Review review);

    @Mapping(source = "name", target = "userName")
    @Mapping(source = "imageUrl", target = "userAvatar")
    ReviewProductUserResponse toReviewProductUserResponse(User user);

    void updateReview(@MappingTarget Review review, ReviewUpdateRequest request);
}
