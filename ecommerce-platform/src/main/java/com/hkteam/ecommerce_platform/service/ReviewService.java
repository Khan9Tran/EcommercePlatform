package com.hkteam.ecommerce_platform.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ReviewResponse;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.useractions.Review;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ReviewMapper;
import com.hkteam.ecommerce_platform.repository.OrderRepository;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.repository.ReviewRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {
    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    AuthenticatedUserUtil authenticatedUserUtil;
    OrderRepository orderRepository;
    ProductRepository productRepository;

    @Transactional
    public ReviewResponse createReview(ReviewCreationRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        var order = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        var latestOrderStatus = order.getOrderStatusHistories().stream()
                .max(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

        if (!"DELIVERED".equals(latestOrderStatus.getOrderStatus().getName())) {
            throw new AppException(ErrorCode.NOT_PURCHASED);
        }

        var products = order.getOrderItems().stream().map(OrderItem::getProduct).collect(Collectors.toSet());

        Review review = reviewMapper.toReview(request);
        review.setUser(user);
        review.setProducts(new ArrayList<>(products));

        try {
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while creating review: " + e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return reviewMapper.toReviewResponse(review);
    }
}
