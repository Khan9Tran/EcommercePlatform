package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.dto.request.*;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.dto.response.OrderCreationResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.payment.Payment;
import com.hkteam.ecommerce_platform.entity.payment.Transaction;
import com.hkteam.ecommerce_platform.entity.payment.TransactionStatusHistory;
import com.hkteam.ecommerce_platform.entity.product.Value;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.entity.status.OrderStatus;
import com.hkteam.ecommerce_platform.entity.status.TransactionStatus;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.enums.PaymentMethod;
import com.hkteam.ecommerce_platform.enums.TransactionStatusName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.OrderItemMapper;
import com.hkteam.ecommerce_platform.mapper.OrderMapper;
import com.hkteam.ecommerce_platform.mapper.OrderStatusHistoryMapper;
import com.hkteam.ecommerce_platform.rabbitmq.RabbitMQConfig;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.OrderUtil;
import com.hkteam.ecommerce_platform.util.PageUtils;
import com.hkteam.ecommerce_platform.util.ShippingFeeUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {
    TransactionStatusRepository transactionStatusRepository;
    PaymentRepository paymentRepository;
    StoreRepository storeRepository;
    OrderRepository orderRepository;
    OrderStatusRepository orderStatusRepository;
    OrderMapper orderMapper;
    OrderItemMapper orderItemMapper;
    OrderStatusHistoryMapper orderStatusHistoryMapper;
    OrderUtil orderUtil;
    PaymentService paymentService;
    AuthenticatedUserUtil authenticatedUserUtil;
    ProductRepository productRepository;
    AddressRepository addressRepository;
    CartItemRepository cartItemRepository;
    CartRepository cartRepository;
    RabbitTemplate rabbitTemplate;
    ProductElasticsearchRepository productElasticsearchRepository;
    VariantRepository variantRepository;

    private static final String ORDER_CODE = "id";
    private static final String CREATED_AT = "createdAt";
    private static final String PHONE = "phone";
    private static final String ADDRESS = "province";
    private static final String GRAND_TOTAL = "grandTotal";
    private static final String ASC = "asc";
    private static final String DESC = "desc";
    private static final String[] SORT_BY_SELLER = {ORDER_CODE, CREATED_AT};
    private static final String[] SORT_BY_ADMIN = {ORDER_CODE, CREATED_AT, PHONE, ADDRESS, GRAND_TOTAL};
    private static final String[] SORT_BY_USER = {CREATED_AT};
    private static final String[] ORDER_BY_SELLER = {ASC, DESC};
    private static final String[] ORDER_BY_ADMIN = {ASC, DESC};
    private static final String[] ORDER_BY_USER = {ASC, DESC};

    @PreAuthorize("hasRole('SELLER')")
    public PaginationResponse<OrderGetAllSellerResponse> getAllOrderBySeller(
            String page, String size, String sortBy, String orderBy, String search, String filter) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (Objects.isNull(user.getStore())) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        String storeId = user.getStore().getId();

        Sort sortable = orderUtil.validateSortAndOrder(sortBy, orderBy, SORT_BY_SELLER, ORDER_BY_SELLER);

        Pageable pageable = PageUtils.createPageable(page, size, sortable);
        var pageData = orderRepository.findAllOrderBySeller(storeId, search, filter, pageable);

        int pageInt = Integer.parseInt(page);

        PageUtils.validatePageBounds(pageInt, pageData);

        List<OrderGetAllSellerResponse> listOrderGetAllSellerResponse = new ArrayList<>();
        pageData.getContent().forEach((order -> {
            OrderStatusHistory lastStatusHistory = orderUtil.getLastOrderStatusHistory(order);

            OrderGetAllSellerResponse orderGetAllSellerResponse = orderMapper.toOrderGetAllSellerResponse(order);

            orderGetAllSellerResponse.setCurrentStatus(
                    lastStatusHistory.getOrderStatus().getName());

            listOrderGetAllSellerResponse.add(orderGetAllSellerResponse);
        }));

        return PaginationResponse.<OrderGetAllSellerResponse>builder()
                .currentPage(pageInt)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? pageInt + 1 : null)
                .previousPage(pageData.hasPrevious() ? pageInt - 1 : null)
                .data(listOrderGetAllSellerResponse)
                .build();
    }

    @PreAuthorize("hasRole('SELLER')")
    public OrderDetailSellerResponse getOneOrderBySeller(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (Objects.isNull(user.getStore())) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }

        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(order))) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_STORE);
        }

        OrderStatusHistory lastStatusHistory = orderUtil.getLastOrderStatusHistory(order);

        List<OrderItemGetOneSellerResponse> listOrderItemGetOneSellerResponse = order.getOrderItems().stream()
                .map(orderItemMapper::toOrderItemGetOneSellerResponse)
                .toList();

        OrderDetailSellerResponse orderDetailSellerResponse = orderMapper.toOrderDetailSellerResponse(order);

        orderDetailSellerResponse.setCurrentStatus(
                lastStatusHistory.getOrderStatus().getName());
        orderDetailSellerResponse.setUserPhone(orderUtil.maskPhone(orderDetailSellerResponse.getUserPhone()));
        orderDetailSellerResponse.setUserEmail(orderUtil.maskEmail(orderDetailSellerResponse.getUserEmail()));
        orderDetailSellerResponse.setOrderItems(listOrderItemGetOneSellerResponse);

        return orderDetailSellerResponse;
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public void updateOneOrderStatusBySeller(String orderId) {
        List<String> listStatus = List.of(
                OrderStatusName.PENDING.name(), OrderStatusName.CONFIRMED.name(), OrderStatusName.PREPARING.name());

        Order order = orderRepository
                .findOneOrderUpdateOrCancel(orderId, listStatus)
                .orElseThrow(() -> new AppException(ErrorCode.ONE_ORDER_UPDATE_STATUS_NOT_FOUND));

        orderUtil.updateOneOrderStatusBySeller(authenticatedUserUtil, order, orderStatusRepository);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while updating order status by seller: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public void updateListOrderStatusBySeller(List<String> listOrderId) {
        List<String> listStatus = List.of(
                OrderStatusName.PENDING.name(), OrderStatusName.CONFIRMED.name(), OrderStatusName.PREPARING.name());

        List<Order> listOrder = orderRepository.findListOrderUpdateOrCancel(listOrderId, listStatus);
        if (listOrder.isEmpty()) {
            throw new AppException(ErrorCode.LIST_ORDER_UPDATE_STATUS_NOT_FOUND);
        }

        listOrder.forEach(
                order -> orderUtil.updateOneOrderStatusBySeller(authenticatedUserUtil, order, orderStatusRepository));

        try {
            orderRepository.saveAll(listOrder);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while updating list order status by seller: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public void cancelOneOrderBySeller(String orderId) {
        List<String> listStatus = List.of(
                OrderStatusName.ON_HOLD.name(),
                OrderStatusName.PENDING.name(),
                OrderStatusName.CONFIRMED.name(),
                OrderStatusName.PREPARING.name());

        Order order = orderRepository
                .findOneOrderUpdateOrCancel(orderId, listStatus)
                .orElseThrow(() -> new AppException(ErrorCode.ONE_ORDER_CANCEL_NOT_FOUND));

        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(order))) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_STORE);
        }

        orderUtil.cancelOneOrder(
                order,
                orderStatusRepository,
                productRepository,
                productElasticsearchRepository,
                variantRepository,
                OrderStatusName.CANCELLED);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling order by seller: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @Transactional
    public void cancelListOrderBySeller(List<String> listOrderId) {
        List<String> listStatus = List.of(
                OrderStatusName.ON_HOLD.name(),
                OrderStatusName.PENDING.name(),
                OrderStatusName.CONFIRMED.name(),
                OrderStatusName.PREPARING.name());

        List<Order> listOrder = orderRepository.findListOrderUpdateOrCancel(listOrderId, listStatus);
        if (listOrder.isEmpty()) {
            throw new AppException(ErrorCode.LIST_ORDER_CANCEL_NOT_FOUND);
        }

        List<Order> filteredListOrder =
                listOrder.stream().filter(authenticatedUserUtil::isOwner).toList();

        if (filteredListOrder.isEmpty()) {
            throw new AppException(ErrorCode.LIST_ORDER_NOT_BELONG_TO_STORE);
        }

        filteredListOrder.forEach(order -> orderUtil.cancelOneOrder(
                order,
                orderStatusRepository,
                productRepository,
                productElasticsearchRepository,
                variantRepository,
                OrderStatusName.CANCELLED));

        try {
            orderRepository.saveAll(filteredListOrder);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling list order by seller: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<OrderGetAllAdminResponse> getAllOrderByAdmin(
            String page, String size, String sortBy, String orderBy, String search, String filter) {
        Sort sortable = orderUtil.validateSortAndOrder(sortBy, orderBy, SORT_BY_ADMIN, ORDER_BY_ADMIN);

        Pageable pageable = PageUtils.createPageable(page, size, sortable);
        var pageData = orderRepository.findAllOrderByAdmin(search, search, search, search, filter, pageable);

        int pageInt = Integer.parseInt(page);

        PageUtils.validatePageBounds(pageInt, pageData);

        List<OrderGetAllAdminResponse> listOrderGetAllAdminResponse = new ArrayList<>();
        pageData.getContent().forEach((order -> {
            OrderStatusHistory lastStatusHistory = orderUtil.getLastOrderStatusHistory(order);
            TransactionStatusHistory lastTransactionStatusHistory = orderUtil.getLastTransactionStatusHistory(order);

            OrderGetAllAdminResponse orderGetAllAdminResponse = orderMapper.toOrderGetAllAdminResponse(order);

            orderGetAllAdminResponse.setCurrentStatus(
                    lastStatusHistory.getOrderStatus().getName());
            orderGetAllAdminResponse.setCurrentStatusTransaction(
                    lastTransactionStatusHistory.getTransactionStatus().getName());

            listOrderGetAllAdminResponse.add(orderGetAllAdminResponse);
        }));

        return PaginationResponse.<OrderGetAllAdminResponse>builder()
                .currentPage(pageInt)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? pageInt + 1 : null)
                .previousPage(pageData.hasPrevious() ? pageInt - 1 : null)
                .data(listOrderGetAllAdminResponse)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public OrderDetailAdminResponse getOneOrderByAdmin(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatusHistory lastStatusHistory = orderUtil.getLastOrderStatusHistory(order);
        TransactionStatusHistory lastTransactionStatusHistory = orderUtil.getLastTransactionStatusHistory(order);

        List<OrderItemGetOneAdminResponse> listOrderItemGetOneAdminResponse = order.getOrderItems().stream()
                .map(orderItemMapper::toOrderItemGetOneAdminResponse)
                .toList();

        OrderDetailAdminResponse orderDetailAdminResponse = orderMapper.toOrderDetailAdminResponse(order);

        orderDetailAdminResponse.setCurrentStatus(
                lastStatusHistory.getOrderStatus().getName());
        orderDetailAdminResponse.setCurrentStatusTransaction(
                lastTransactionStatusHistory.getTransactionStatus().getName());
        orderDetailAdminResponse.setOrderItems(listOrderItemGetOneAdminResponse);
        orderUtil.addStoreAddress(order, addressRepository, orderDetailAdminResponse);

        return orderDetailAdminResponse;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void updateOneOrderStatusByAdmin(String orderId) {
        List<String> listStatus = List.of(
                OrderStatusName.WAITING_FOR_SHIPPING.name(),
                OrderStatusName.PICKED_UP.name(),
                OrderStatusName.OUT_FOR_DELIVERY.name());

        Order order = orderRepository
                .findOneOrderUpdateOrCancel(orderId, listStatus)
                .orElseThrow(() -> new AppException(ErrorCode.ONE_ORDER_UPDATE_STATUS_NOT_FOUND));

        orderUtil.updateOneOrderStatusByAdmin(order, orderStatusRepository, transactionStatusRepository);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while updating order status by admin: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void updateListOrderStatusByAdmin(List<String> listOrderId) {
        List<String> listStatus = List.of(
                OrderStatusName.WAITING_FOR_SHIPPING.name(),
                OrderStatusName.PICKED_UP.name(),
                OrderStatusName.OUT_FOR_DELIVERY.name());

        List<Order> listOrder = orderRepository.findListOrderUpdateOrCancel(listOrderId, listStatus);
        if (listOrder.isEmpty()) {
            throw new AppException(ErrorCode.LIST_ORDER_UPDATE_STATUS_NOT_FOUND);
        }

        listOrder.forEach(order ->
                orderUtil.updateOneOrderStatusByAdmin(order, orderStatusRepository, transactionStatusRepository));

        try {
            orderRepository.saveAll(listOrder);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while updating list order status by admin: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void cancelOneOrderByAdmin(String orderId) {
        List<String> listStatus = List.of(
                OrderStatusName.ON_HOLD.name(),
                OrderStatusName.PENDING.name(),
                OrderStatusName.CONFIRMED.name(),
                OrderStatusName.PREPARING.name(),
                OrderStatusName.WAITING_FOR_SHIPPING.name(),
                OrderStatusName.PICKED_UP.name(),
                OrderStatusName.OUT_FOR_DELIVERY.name());

        Order order = orderRepository
                .findOneOrderUpdateOrCancel(orderId, listStatus)
                .orElseThrow(() -> new AppException(ErrorCode.ONE_ORDER_CANCEL_NOT_FOUND));

        orderUtil.cancelOneOrder(
                order,
                orderStatusRepository,
                productRepository,
                productElasticsearchRepository,
                variantRepository,
                OrderStatusName.CANCELLED);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling order by admin: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void cancelListOrderByAdmin(List<String> orderId) {
        List<String> listStatus = List.of(
                OrderStatusName.ON_HOLD.name(),
                OrderStatusName.PENDING.name(),
                OrderStatusName.CONFIRMED.name(),
                OrderStatusName.PREPARING.name(),
                OrderStatusName.WAITING_FOR_SHIPPING.name(),
                OrderStatusName.PICKED_UP.name(),
                OrderStatusName.OUT_FOR_DELIVERY.name());

        List<Order> listOrder = orderRepository.findListOrderUpdateOrCancel(orderId, listStatus);
        if (listOrder.isEmpty()) {
            throw new AppException(ErrorCode.LIST_ORDER_CANCEL_NOT_FOUND);
        }

        listOrder.forEach(order -> orderUtil.cancelOneOrder(
                order,
                orderStatusRepository,
                productRepository,
                productElasticsearchRepository,
                variantRepository,
                OrderStatusName.CANCELLED));

        try {
            orderRepository.saveAll(listOrder);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling list order by admin: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('USER')")
    public PaginationResponse<OrderGetAllUserResponse> getAllOrderByUser(
            String page, String size, String sortBy, String orderBy, String search, String filter) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        String userId = user.getId();

        Sort sortable = orderUtil.validateSortAndOrder(sortBy, orderBy, SORT_BY_USER, ORDER_BY_USER);

        Pageable pageable = PageUtils.createPageable(page, size, sortable);
        var pageData = orderRepository.findAllOrderByUser(userId, search, search, search, filter, pageable);

        int pageInt = Integer.parseInt(page);

        PageUtils.validatePageBounds(pageInt, pageData);

        List<OrderGetAllUserResponse> listOrderGetAllUserResponse = new ArrayList<>();
        pageData.getContent().forEach((order -> {
            OrderStatusHistory lastStatusHistory = orderUtil.getLastOrderStatusHistory(order);
            TransactionStatusHistory lastTransactionStatusHistory = orderUtil.getLastTransactionStatusHistory(order);

            List<OrderItemGetAllUserResponse> listOrderItemGetAllUserResponse = order.getOrderItems().stream()
                    .map(orderItemMapper::toOrderItemGetAllUserResponse)
                    .toList();

            OrderGetAllUserResponse orderGetAllUserResponse = orderMapper.toOrderGetAllUserResponse(order);

            orderGetAllUserResponse.setCurrentStatus(
                    lastStatusHistory.getOrderStatus().getName());
            orderGetAllUserResponse.setCurrentStatusTransaction(
                    lastTransactionStatusHistory.getTransactionStatus().getName());
            orderGetAllUserResponse.setOrderItems(listOrderItemGetAllUserResponse);

            listOrderGetAllUserResponse.add(orderGetAllUserResponse);
        }));

        return PaginationResponse.<OrderGetAllUserResponse>builder()
                .currentPage(pageInt)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? pageInt + 1 : null)
                .previousPage(pageData.hasPrevious() ? pageInt - 1 : null)
                .data(listOrderGetAllUserResponse)
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    public OrderGetOneUserResponse getOneOrderByUser(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        String defaultAddressStr = String.format(
                "%s%s, %s, %s, %s",
                order.getDetailLocate() != null ? order.getDetailLocate() + ", " : "",
                order.getDetailAddress(),
                order.getDistrict(),
                order.getSubDistrict(),
                order.getProvince());

        OrderStatusHistory lastStatusHistory = orderUtil.getLastOrderStatusHistory(order);

        List<OrderItemGetOneUserResponse> listOrderItemGetOneUserResponse = order.getOrderItems().stream()
                .map(orderItemMapper::toOrderItemGetOneUserResponse)
                .toList();

        List<OrderStatusHistory> sortedOrderStatusHistories = order.getOrderStatusHistories().stream()
                .sorted(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                .toList();
        List<OrderStatusHistoryGetOneUserResponse> listOrderStatusHistoryGetOneUserResponse =
                orderStatusHistoryMapper.toListOrderStatusHistoryGetOneUserResponse(sortedOrderStatusHistories);

        OrderGetOneUserResponse orderGetOneUserResponse = orderMapper.toOrderGetOneUserResponse(order);

        orderGetOneUserResponse.setDefaultAddressStr(defaultAddressStr);
        orderGetOneUserResponse.setCurrentStatus(
                lastStatusHistory.getOrderStatus().getName());
        orderGetOneUserResponse.setOrderItems(listOrderItemGetOneUserResponse);
        orderGetOneUserResponse.setOrderStatusHistories(listOrderStatusHistoryGetOneUserResponse);

        return orderGetOneUserResponse;
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public void cancelOneOrderByUser(String orderId) {
        List<String> listStatus = List.of(OrderStatusName.ON_HOLD.name());

        Order order = orderRepository
                .findOneOrderUpdateOrCancel(orderId, listStatus)
                .orElseThrow(() -> new AppException(ErrorCode.ONE_ORDER_CANCEL_NOT_FOUND));

        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        orderUtil.cancelOneOrder(
                order,
                orderStatusRepository,
                productRepository,
                productElasticsearchRepository,
                variantRepository,
                OrderStatusName.CANCELLED);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling order by user: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 10, backoff = @Backoff(delay = 100))
    @Transactional
    public OrderCreationResponse createOrder(ListOrder listOrder, HttpServletRequest request) {
        boolean isVnPay = listOrder.getPaymentMethod().equals(PaymentMethod.VN_PAY);
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal shippingFee = BigDecimal.ZERO;

        Payment payment = Payment.builder().paymentDetails("Payment for orders").build();

        Set<Transaction> transactions = new HashSet<>();

        var user = authenticatedUserUtil.getAuthenticatedUser();
        var address = addressRepository
                .findById(listOrder.getAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        for (OrderRequest orderRequest : listOrder.getOrders()) {

            Store store = storeRepository
                    .findById(orderRequest.getStoreId())
                    .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

            BigDecimal totalOriginalPrice = totalPaymentBeforeSale(orderRequest);
            BigDecimal totalSalePrice = totalSalePrice(orderRequest);

            amount = amount.add(ShippingFeeUtil.calculateShippingFee());
            amount = amount.add(totalSalePrice);
            total = total.add(totalOriginalPrice);
            discount = discount.add(totalOriginalPrice.subtract(totalSalePrice));
            shippingFee = shippingFee.add(ShippingFeeUtil.calculateShippingFee());

            BigDecimal verifyTotalOriginalPrice = BigDecimal.ZERO;
            BigDecimal verifyTotalSalePrice = BigDecimal.ZERO;

            List<OrderItem> orderItems = new ArrayList<>();

            for (OrderItemRequest orderItemRequest : orderRequest.getOrderItems()) {
                var product = productRepository
                        .findById(orderItemRequest.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                var cartItem = cartItemRepository
                        .findById(orderItemRequest.getCartItemId())
                        .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

                if (!product.getStore().equals(store)) throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

                if (product.getQuantity() < orderItemRequest.getQuantity())
                    throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);

                if (product.isBlocked() || Boolean.FALSE.equals(product.isAvailable()))
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

                Variant variant = null;
                Boolean hasVariant = Boolean.FALSE.equals(product.getVariants().isEmpty());

                if (Objects.isNull(orderItemRequest.getVariantId())) {
                    if (Boolean.TRUE.equals(hasVariant)) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);

                    verifyTotalOriginalPrice = verifyTotalOriginalPrice.add(
                            product.getOriginalPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));
                    verifyTotalSalePrice = verifyTotalSalePrice.add(
                            product.getSalePrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));

                } else {
                    if (Boolean.FALSE.equals(hasVariant)) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);

                    variant = product.getVariants().stream()
                            .filter(v -> v.getId().equals(orderItemRequest.getVariantId()))
                            .findFirst()
                            .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                    if (variant.getQuantity() < orderItemRequest.getQuantity())
                        throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);

                    verifyTotalOriginalPrice = verifyTotalOriginalPrice.add(
                            variant.getOriginalPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));
                    verifyTotalSalePrice = verifyTotalSalePrice.add(
                            variant.getSalePrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));
                }

                product.setQuantity(product.getQuantity() - orderItemRequest.getQuantity());
                productElasticsearchRepository
                        .findById(product.getId())
                        .ifPresentOrElse(
                                productElasticsearch -> {
                                    productElasticsearch.setQuantity(product.getQuantity());
                                    productElasticsearchRepository.save(productElasticsearch);
                                },
                                () -> log.error("Product not found in elasticsearch."));

                if (Boolean.TRUE.equals(hasVariant)) {
                    variant.setQuantity(variant.getQuantity() - orderItemRequest.getQuantity());
                }
                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .price(hasVariant ? variant.getOriginalPrice() : product.getOriginalPrice())
                        .discount(
                                hasVariant
                                        ? variant.getOriginalPrice().subtract(variant.getSalePrice())
                                        : product.getOriginalPrice().subtract(product.getSalePrice()))
                        .quantity(orderItemRequest.getQuantity())
                        .values(
                                Boolean.FALSE.equals(hasVariant)
                                        ? null
                                        : variant.getValues().stream()
                                                .map(Value::getValue)
                                                .toList())
                        .build();
                orderItems.add(orderItem);
                cartItem.setCheckout(Boolean.TRUE);
                cartItemRepository.save(cartItem);
            }

            if (Boolean.FALSE.equals(totalOriginalPrice
                            .stripTrailingZeros()
                            .equals(verifyTotalOriginalPrice.stripTrailingZeros()))
                    || Boolean.FALSE.equals(
                            totalSalePrice.stripTrailingZeros().equals(verifyTotalSalePrice.stripTrailingZeros())))
                throw new AppException(ErrorCode.UNKNOWN_ERROR);

            log.info("Order created successfully.");
            OrderStatus orderStatus;
            if (isVnPay)
                orderStatus = orderStatusRepository
                        .findByName(OrderStatusName.ON_HOLD.name())
                        .orElseThrow(() -> new AppException(ErrorCode.UNKNOWN_ERROR));
            else
                orderStatus = orderStatusRepository
                        .findByName(OrderStatusName.PENDING.name())
                        .orElseThrow(() -> new AppException(ErrorCode.UNKNOWN_ERROR));

            OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                    .orderStatus(orderStatus)
                    .remarks("Process order auto by HKUpTech")
                    .build();

            Order order = Order.builder()
                    .store(store)
                    .user(user)
                    .total(totalOriginalPrice)
                    .discount(totalOriginalPrice.subtract(totalSalePrice))
                    .phone(address.getPhone())
                    .recipientName(address.getRecipientName())
                    .district(address.getDistrict())
                    .subDistrict(address.getSubDistrict())
                    .detailAddress(address.getDetailAddress())
                    .shippingFee(ShippingFeeUtil.calculateShippingFee())
                    .detailLocate(address.getDetailLocate())
                    .province(address.getProvince())
                    .note(listOrder.getNote())
                    .grandTotal(totalSalePrice.add(ShippingFeeUtil.calculateShippingFee()))
                    .promo(totalOriginalPrice.subtract(totalSalePrice))
                    .shippingDiscount(BigDecimal.ZERO)
                    .orderItems(orderItems)
                    .shippingTotal(ShippingFeeUtil.calculateShippingFee())
                    .orderStatusHistories(new ArrayList<>(List.of(orderStatusHistory)))
                    .build();

            for (OrderItem item : orderItems) {
                item.setOrder(order);
            }

            orderStatusHistory.setOrder(order);

            TransactionStatus pending = transactionStatusRepository
                    .findById(TransactionStatusName.WAITING.name())
                    .orElseThrow(() -> new AppException(ErrorCode.UNKNOWN_ERROR));

            TransactionStatusHistory transactionStatusHistory = TransactionStatusHistory.builder()
                    .transactionStatus(pending)
                    .remarks("Process payment.")
                    .build();

            Transaction transaction = Transaction.builder()
                    .payment(payment)
                    .transactionStatusHistories(List.of(transactionStatusHistory))
                    .order(order)
                    .amount(amount)
                    .build();

            transactionStatusHistory.setTransaction(transaction);
            transactions.add(transaction);
        }

        payment.setTransactions(transactions);
        payment.setAmount(amount);
        payment.setPaymentMethod(isVnPay ? PaymentMethod.VN_PAY : PaymentMethod.COD);

        paymentRepository.save(payment);

        listOrder.getOrders().forEach((order) -> {
            Cart cart = cartRepository
                    .findByUserAndStoreId(user, order.getStoreId())
                    .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
            var rs = cart.getCartItems().stream()
                    .filter(cartItem -> !cartItem.isCheckout())
                    .toList();
            if (rs.isEmpty()) {
                cart.setAvailable(Boolean.FALSE);
                cartRepository.save(cart);
            }
        });

        if (Objects.nonNull(user.getEmail()) && !user.getEmail().isBlank()) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SEND_MAIL_AFTER_ORDER_QUEUE,
                    SendMailAfterOrderRequest.builder()
                            .email(user.getEmail())
                            .name(user.getName())
                            .paymentId(payment.getId())
                            .build());
        }

        return OrderCreationResponse.builder()
                .paymentUrl(
                        isVnPay
                                ? paymentService.createVnPayPayment(amount, request, payment.getId())
                                : "Please pay upon receiving the goods.")
                .status("Success")
                .paymentId(payment.getId())
                .build();
    }

    private BigDecimal totalSalePrice(OrderRequest request) {
        BigDecimal fee = BigDecimal.ZERO;

        for (OrderItemRequest orderItemRequest : request.getOrderItems()) {
            fee = fee.add(orderItemRequest.getSalePrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));
        }

        return fee;
    }

    private BigDecimal totalPaymentBeforeSale(OrderRequest request) {
        BigDecimal fee = BigDecimal.ZERO;

        for (OrderItemRequest orderItemRequest : request.getOrderItems()) {
            fee = fee.add(
                    orderItemRequest.getOriginalPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));
        }
        return fee;
    }

    @Recover
    public OrderCreationResponse recover(
            OptimisticLockingFailureException e, ListOrder listOrder, HttpServletRequest request) {
        log.error("Recovering after retries failed: {}", e.getMessage());
        throw new AppException(ErrorCode.RETRY_FAILED);
    }

    @Recover
    public OrderCreationResponse recover(Exception e, ListOrder listOrder, HttpServletRequest request)
            throws Exception {
        log.error("Unexpected error occurred: {}", e.getMessage());
        throw e;
    }
}
