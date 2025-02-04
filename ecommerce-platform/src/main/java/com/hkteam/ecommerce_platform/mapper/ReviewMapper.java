package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ReviewCreationResponse;
import com.hkteam.ecommerce_platform.dto.response.ReviewListOneProductResponse;
import com.hkteam.ecommerce_platform.dto.response.ReviewOrderItemResponse;
import com.hkteam.ecommerce_platform.dto.response.ReviewProductUserResponse;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.entity.useractions.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toReview(ReviewCreationRequest request);

    ReviewCreationResponse toReviewCreationResponse(Review review);

    ReviewListOneProductResponse toReviewListOneProductResponse(Review review);

    @Mapping(source = "name", target = "userName")
    @Mapping(source = "imageUrl", target = "userAvatar")
    ReviewProductUserResponse toReviewProductUserResponse(User user);

    @Mapping(source = "product.slug", target = "productSlug")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    ReviewOrderItemResponse toReviewOrderItemResponse(OrderItem orderItem);
}
