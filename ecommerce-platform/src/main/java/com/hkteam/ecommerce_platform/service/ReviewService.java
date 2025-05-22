package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.hkteam.ecommerce_platform.dto.request.StatisticRequest;
import com.hkteam.ecommerce_platform.util.DateRangeUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.dto.request.ReviewCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.image.ReviewImage;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.status.OrderStatus;
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
    StoreRepository storeRepository;
    ReviewImageRepository reviewImageRepository;

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

    @PreAuthorize("hasRole('USER')")
    public List<ReviewStoreResponse> getAllReviewByStoreId(String storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        User user = authenticatedUserUtil.getAuthenticatedUser();

        List<Review> listReview = reviewRepository.findAllByUserId(user.getId());
        if (Objects.isNull(listReview) || listReview.isEmpty()) {
            throw new AppException(ErrorCode.LIST_REVIEW_NOT_FOUND);
        }

        List<ReviewStoreResponse> listReviewStoreResponse = new ArrayList<>();

        listReview.forEach(review -> review.getProducts().stream()
                .filter(product -> product.getStore().getId().equals(store.getId()))
                .forEach(product -> {
                    Set<ReviewListValueStoreResponse> listReviewListValueStoreResponse = new HashSet<>();

                    product.getOrderItems().stream()
                            .filter(orderItem -> {
                                Order order = orderItem.getOrder();
                                return order.getUser().getId().equals(user.getId())
                                        && order.getOrderStatusHistories().stream()
                                                .max(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                                                .map(OrderStatusHistory::getOrderStatus)
                                                .map(OrderStatus::getName)
                                                .filter(name -> name.equals(OrderStatusName.DELIVERED.toString()))
                                                .isPresent();
                            })
                            .forEach(orderItem -> {
                                ReviewListValueStoreResponse reviewListValueStoreResponse =
                                        new ReviewListValueStoreResponse();

                                reviewListValueStoreResponse.setValues(orderItem.getValues());

                                listReviewListValueStoreResponse.add(reviewListValueStoreResponse);
                            });

                    List<ReviewListImageResponse> listReviewListImageResponse = new ArrayList<>();

                    List<ReviewImage> listReviewImage = reviewImageRepository.findAllByReviewId(review.getId());
                    listReviewImage.forEach(reviewImage -> {
                        ReviewListImageResponse reviewListImageResponse = new ReviewListImageResponse();

                        reviewListImageResponse.setUrl(reviewImage.getUrl());

                        listReviewListImageResponse.add(reviewListImageResponse);
                    });

                    ReviewStoreResponse reviewStoreResponse = new ReviewStoreResponse();

                    reviewStoreResponse.setProductMainImageUrl(product.getMainImageUrl());
                    reviewStoreResponse.setProductName(product.getName());
                    reviewStoreResponse.setProductValues(new ArrayList<>(listReviewListValueStoreResponse));
                    reviewStoreResponse.setProductSlug(product.getSlug());
                    reviewStoreResponse.setUserName(review.getUser().getName());
                    reviewStoreResponse.setUserAccount(review.getUser().getUsername());
                    reviewStoreResponse.setUserAvatar(review.getUser().getImageUrl());
                    reviewStoreResponse.setReviewRating(review.getRating());
                    reviewStoreResponse.setReviewComment(review.getComment());
                    reviewStoreResponse.setReviewVideo(review.getVideoUrl());
                    reviewStoreResponse.setReviewImages(listReviewListImageResponse);
                    reviewStoreResponse.setLastUpdatedAt(review.getLastUpdatedAt());

                    listReviewStoreResponse.add(reviewStoreResponse);
                }));

        return listReviewStoreResponse;
    }

    public boolean isAllOrderReviewed(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        User user = authenticatedUserUtil.getAuthenticatedUser();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        List<Product> listProduct =
                order.getOrderItems().stream().map(OrderItem::getProduct).toList();

        return listProduct.stream()
                .allMatch(product -> reviewRepository.hasUserAlreadyReviewedProduct(user.getId(), product.getId()));
    }

    public boolean isAnyOrderReviewed(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        User user = authenticatedUserUtil.getAuthenticatedUser();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        List<Product> listProduct =
                order.getOrderItems().stream().map(OrderItem::getProduct).toList();

        return listProduct.stream()
                .anyMatch(product -> reviewRepository.hasUserAlreadyReviewedProduct(user.getId(), product.getId()));
    }

    public List<ReviewOrderItemResponse> getAllProductReview(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        User user = authenticatedUserUtil.getAuthenticatedUser();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        List<OrderItem> listOrderItem = order.getOrderItems().stream()
                .filter(orderItem -> !reviewRepository.hasUserAlreadyReviewedProduct(
                        user.getId(), orderItem.getProduct().getId()))
                .toList();

        List<ReviewOrderItemResponse> listReviewOrderItemResponse = new ArrayList<>();

        listOrderItem.forEach(orderItem -> {
            ReviewOrderItemResponse reviewOrderItemResponse = reviewMapper.toReviewOrderItemResponse(orderItem);

            listReviewOrderItemResponse.add(reviewOrderItemResponse);
        });

        return listReviewOrderItemResponse;
    }

    public StatisticResponse getStatistics(StatisticRequest request) {
        log.info("Get statistics: {}", request);
        LocalDate[] resolvedRange = DateRangeUtil.resolveFromTo(
                request.getRangeType(),
                request.getFrom(),
                request.getTo()
        );

        Instant[] range = DateRangeUtil.convertLocalDateRangeToInstant(resolvedRange);

        Instant from = range[0];
        Instant to = range[1];



        if (!request.getType().equals("REVENUE") && !request.getType().equals("PRODUCT_SOLD") && !request.getType().equals("ORDER_COUNT")) {
            throw new AppException(ErrorCode.TAB_INVALID);
        }

        String storeId = request.getStoreId();
        String productId = request.getProductId();
        String groupBy = request.getGroupBy().getPostgresFormat();
        String type = request.getType();


        if (type.equals("REVENUE")) {

            List<Object[]> results = orderRepository.getRevenueStatisticsNative(
                    from,
                    to,
                    groupBy,
                    storeId,
                    productId,
                    request.getOffset(),
                    request.getLimit()

            );

            List<Object[]> rs = orderRepository.getRevenueStatisticsNativeNoPage(
                    from,
                    to,
                    groupBy,
                    storeId,
                    productId
            );

            Integer count = rs.size();

            BigDecimal sum = results.stream()
                    .map(row -> row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<StatisticItem> items = results.stream().map(row -> new StatisticItem(
                    row[0], // groupKey (String)
                    "",     // entityId
                    "revenue",     // entityName
                    "",     // slug
                    "",     // imageUrl
                    0,      // quantity
                    "VNĐ",
                    row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO
            )).toList();

            return StatisticResponse.builder()
                    .data(items)
                    .totalCount(0)
                    .totalItems(count)
                    .totalAmount(sum)
                    .build();
        }

        return null;
    }

}
