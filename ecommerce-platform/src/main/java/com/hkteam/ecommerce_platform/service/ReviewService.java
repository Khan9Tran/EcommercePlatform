package com.hkteam.ecommerce_platform.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.entity.useractions.Review;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ReviewMapper;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.PageUtils;
import com.hkteam.ecommerce_platform.util.ReviewUtil;

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
    ProductElasticsearchRepository productElasticsearchRepository;
    ReviewUtil reviewUtil;

    private static final String[] SORT_BY = {"createdAt"};
    private static final String[] ORDER_BY = {"asc", "desc"};

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public ReviewCreationResponse createReview(ReviewCreationRequest request) {
        User user = authenticatedUserUtil.getAuthenticatedUser();

        Order order = orderRepository
                .findOrderById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        OrderStatusHistory latestOrderStatus = order.getOrderStatusHistories().stream()
                .max(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

        if (!(OrderStatusName.DELIVERED.toString())
                .equals(latestOrderStatus.getOrderStatus().getName())) {
            throw new AppException(ErrorCode.NOT_PURCHASED);
        }

        Set<Product> products = order.getOrderItems().stream()
                .map(OrderItem::getProduct)
                .filter(product -> !reviewRepository.hasUserAlreadyReviewedProduct(user.getId(), product.getId()))
                .collect(Collectors.toSet());
        if (products.isEmpty()) {
            throw new AppException(ErrorCode.REVIEWED_PRODUCT);
        }

        Review review = reviewMapper.toReview(request);
        review.setUser(user);
        review.setProducts(new ArrayList<>(products));

        try {
            for (Product product : products) {
                updateProductRating(product, request.getRating());
            }

            reviewRepository.save(review);

            Store store = products.stream().toList().getFirst().getStore();
            double newRating = store.getProducts().stream()
                    .map((Product::getRating))
                    .filter((Objects::nonNull))
                    .mapToDouble(Float::doubleValue)
                    .average()
                    .orElse(0.0);

            store.setRating(((float) newRating));
        } catch (DataIntegrityViolationException e) {
            log.error("Error while creating review: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return reviewMapper.toReviewCreationResponse(review);
    }

    private void updateProductRating(Product product, float ratingNew) {
        var esPro = productElasticsearchRepository
                .findById(product.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        float rating;
        if (Objects.isNull(product.getRating())) {
            rating = ratingNew;
        } else {
            int numberOfRating = product.getReviews().size();
            rating = (product.getRating() * numberOfRating + ratingNew) / (numberOfRating + 1);
        }

        product.setRating(rating);
        esPro.setRating(rating);

        productRepository.save(product);
        productElasticsearchRepository.save(esPro);
    }

    public PaginationResponse<ReviewOneProductResponse> getReviewOneProduct(
            String productId,
            String starNumber,
            String commentString,
            String mediaString,
            String page,
            String size,
            String sortBy,
            String orderBy) {
        var product =
                productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Sort sortable = reviewUtil.validateSortAndOrder(sortBy, orderBy, SORT_BY, ORDER_BY);

        Pageable pageable = PageUtils.createPageable(page, size, sortable);
        var pageData = reviewRepository.findAllReviewProductId(
                productId,
                starNumber,
                commentString.isEmpty() ? "" : "commentString",
                mediaString.isEmpty() ? "" : "mediaString",
                pageable);

        int pageInt = Integer.parseInt(page);

        PageUtils.validatePageBounds(pageInt, pageData);

        List<ReviewListOneProductResponse> listReviewResponse = pageData.stream()
                .map(review -> {
                    List<Product> listProduct = review.getProducts();
                    List<ReviewListValueProductResponse> productValues = new ArrayList<>();

                    listProduct.stream()
                            .filter(pro -> pro.getId().equals(productId))
                            .forEach(pro -> {
                                List<OrderItem> listOrderItem = pro.getOrderItems().stream()
                                        .filter(orderItem ->
                                                orderItem.getOrder().getUser().equals(review.getUser()))
                                        .toList();

                                listOrderItem.forEach(orderItem -> {
                                    List<String> values = orderItem.getValues();
                                    ReviewListValueProductResponse reviewListValueProductResponse =
                                            new ReviewListValueProductResponse(values);
                                    productValues.add(reviewListValueProductResponse);
                                });
                            });

                    ReviewListOneProductResponse response = reviewMapper.toReviewListOneProductResponse(review);
                    response.setProductValues(productValues);
                    response.setUser(reviewMapper.toReviewProductUserResponse(review.getUser()));

                    return response;
                })
                .toList();

        List<Object[]> ratingCounts = reviewRepository.countReviewsByRating(productId);
        Map<Float, Long> ratingCountMap = new HashMap<>();
        for (Object[] row : ratingCounts) {
            Float rating = ((Number) row[0]).floatValue();
            Long count = (Long) row[1];
            ratingCountMap.put(rating, count);
        }

        RatingCountResponse ratingCountResponse = RatingCountResponse.builder()
                .fiveStar(ratingCountMap.getOrDefault(5f, 0L))
                .fourStar(ratingCountMap.getOrDefault(4f, 0L))
                .threeStar(ratingCountMap.getOrDefault(3f, 0L))
                .twoStar(ratingCountMap.getOrDefault(2f, 0L))
                .oneStar(ratingCountMap.getOrDefault(1f, 0L))
                .build();

        ReviewOneProductResponse reviewOneProductResponse = ReviewOneProductResponse.builder()
                .productRating(product.getRating() != null ? product.getRating() : 0)
                .reviews(listReviewResponse)
                .ratingCounts(ratingCountResponse)
                .build();
        List<ReviewOneProductResponse> listReviewOneProductResponse = new ArrayList<>();
        listReviewOneProductResponse.add(reviewOneProductResponse);

        return PaginationResponse.<ReviewOneProductResponse>builder()
                .data(listReviewOneProductResponse)
                .currentPage(Integer.parseInt(page))
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? pageInt + 1 : null)
                .previousPage(pageData.hasPrevious() ? pageInt - 1 : null)
                .build();
    }

    public ReviewCountResponse getCommentAndMediaTotalReview(String productId) {
        List<Review> listReview = reviewRepository.findAllReviewByProductId(productId);

        long totalComments = listReview.stream()
                .filter(review ->
                        review.getComment() != null && !review.getComment().isEmpty())
                .count();

        long totalWithMedia = listReview.stream()
                .filter(review -> (review.getVideoUrl() != null
                                && !review.getVideoUrl().isEmpty())
                        || (review.getImages() != null && !review.getImages().isEmpty()))
                .count();

        return ReviewCountResponse.builder()
                .totalComments(totalComments)
                .totalWithMedia(totalWithMedia)
                .build();
    }
}
