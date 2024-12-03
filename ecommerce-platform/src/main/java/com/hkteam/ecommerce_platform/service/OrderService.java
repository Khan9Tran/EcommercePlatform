package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;

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

import com.hkteam.ecommerce_platform.dto.request.ListOrder;
import com.hkteam.ecommerce_platform.dto.request.OrderItemRequest;
import com.hkteam.ecommerce_platform.dto.request.OrderRequest;
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
import com.hkteam.ecommerce_platform.mapper.ProductMapper;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
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
    PaymentService paymentService;
    AuthenticatedUserUtil authenticatedUserUtil;
    ProductRepository productRepository;
    AddressRepository addressRepository;
    CartItemRepository cartItemRepository;
    CartRepository cartRepository;
    ProductMapper productMapper;

    static String[] SORT_BY_SELLER = {"createdAt"};
    static String[] SORT_BY_ADMIN = {"createdAt"};
    static String[] SORT_BY_USER = {"createdAt"};
    static String[] ORDER_BY = {"asc", "desc"};

    @PreAuthorize("hasRole('SELLER')")
    public PaginationResponse<OrderResponseSeller> getAllOrderBySeller(
            String pageStr, String sizeStr, String sortBy, String orderBy, String search, String filter) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (user.getStore() == null) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        String storeId = user.getStore().getId();

        if (!Arrays.asList(SORT_BY_SELLER).contains(sortBy)) sortBy = null;
        if (!Arrays.asList(ORDER_BY).contains(orderBy)) orderBy = null;
        Sort sortable = (sortBy == null || orderBy == null)
                ? Sort.unsorted()
                : Sort.by(Sort.Direction.fromString(orderBy), sortBy);

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        var pageData = orderRepository.findAllOrderByStore(storeId, search, filter, pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);
        List<OrderResponseSeller> orderResponseSellers = new ArrayList<>();
        pageData.getContent().forEach((order -> {
            OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().stream()
                    .max(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                    .orElseThrow(() -> new AppException(ErrorCode.STATUS_HISTORY_NOT_FOUND));

            OrderResponseSeller orderResponseSeller = orderMapper.toOrderResponseSeller(order);

            orderResponseSeller.setCurrentStatus(
                    lastStatusHistory.getOrderStatus().getName());
            orderResponseSeller.setUserPhone(maskPhone(orderResponseSeller.getUserPhone()));
            orderResponseSeller.setUserEmail(maskEmail(orderResponseSeller.getUserEmail()));

            List<OrderItemResponseSeller> orderItemResponseSellers =
                    orderItemMapper.toOrderItemResponseSellerList(order.getOrderItems());
            orderResponseSeller.setOrderItems(orderItemResponseSellers);
            orderResponseSellers.add(orderResponseSeller);
        }));

        return PaginationResponse.<OrderResponseSeller>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(orderResponseSellers)
                .build();
    }

    private String maskPhone(String phone) {
        if (phone != null && phone.length() > 3) {
            return phone.substring(0, phone.length() - 3) + "***";
        }
        return phone;
    }

    private String maskEmail(String email) {
        if (email != null && email.contains("@")) {
            int atIndex = email.indexOf("@");
            if (atIndex > 3) {
                return email.substring(0, atIndex - 3) + "***" + email.substring(atIndex);
            }
        }
        return email;
    }

    @PreAuthorize("hasRole('SELLER')")
    public OrderResponseSeller getOneOrderBySeller(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (user.getStore() == null) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }

        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(order))) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_STORE);
        }

        OrderResponseSeller orderResponseSeller = orderMapper.toOrderResponseSeller(order);
        orderResponseSeller.setCurrentStatus(
                order.getOrderStatusHistories().getLast().getOrderStatus().getName());
        orderResponseSeller.setUserPhone(maskPhone(orderResponseSeller.getUserPhone()));
        orderResponseSeller.setUserEmail(maskEmail(orderResponseSeller.getUserEmail()));
        List<OrderItemResponseSeller> orderItemResponseSellers =
                orderItemMapper.toOrderItemResponseSellerList(order.getOrderItems());
        orderResponseSeller.setOrderItems(orderItemResponseSellers);

        return orderResponseSeller;
    }

    @PreAuthorize("hasRole('SELLER')")
    public void updateOrderStatusBySeller(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(order))) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_STORE);
        }

        OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().getLast();
        OrderStatusName currentStatus =
                OrderStatusName.valueOf(lastStatusHistory.getOrderStatus().getName());
        OrderStatusName nextStatusName = getNextStatusSeller(currentStatus);
        OrderStatus nextStatus = orderStatusRepository
                .findByName(nextStatusName.name())
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

        order.getOrderStatusHistories()
                .add(OrderStatusHistory.builder()
                        .order(order)
                        .orderStatus(nextStatus)
                        .remarks(order.getOrderStatusHistories().getLast().getRemarks())
                        .build());
        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while updating order status: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    public void cancelOrderBySeller(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(order))) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_STORE);
        }

        OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().getLast();
        if (OrderStatusName.CANCELLED
                .name()
                .equals(lastStatusHistory.getOrderStatus().getName())) {
            throw new AppException(ErrorCode.ORDER_CANCELLED);
        }

        OrderStatus cancelledStatus = orderStatusRepository
                .findByName(OrderStatusName.CANCELLED.name())
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

        OrderStatusHistory cancelledStatusHistory = OrderStatusHistory.builder()
                .order(order)
                .orderStatus(cancelledStatus)
                .remarks(order.getOrderStatusHistories().getLast().getRemarks())
                .build();

        order.getOrderStatusHistories().add(cancelledStatusHistory);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling order: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    private OrderStatusName getNextStatusSeller(OrderStatusName currentStatus) {
        return switch (currentStatus) {
            case PENDING -> OrderStatusName.CONFIRMED;
            case CONFIRMED -> OrderStatusName.PREPARING;
            case PREPARING -> OrderStatusName.WAITING_FOR_SHIPPING;
            default -> throw new AppException(ErrorCode.NOT_PERMISSION_ORDER);
        };
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<OrderResponseAdmin> getAllOrderByAdmin(
            String pageStr, String sizeStr, String sortBy, String orderBy, String search, String filter) {

        if (!Arrays.asList(SORT_BY_ADMIN).contains(sortBy)) sortBy = null;
        if (!Arrays.asList(ORDER_BY).contains(orderBy)) orderBy = null;
        Sort sortable = (sortBy == null || orderBy == null)
                ? Sort.unsorted()
                : Sort.by(Sort.Direction.fromString(orderBy), sortBy);

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        var pageData = orderRepository.findAllOrderByAdmin(search, search, search, filter, pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);
        List<OrderResponseAdmin> orderResponseAdmins = new ArrayList<>();
        pageData.getContent().forEach((order -> {
            OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().stream()
                    .max(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                    .orElseThrow(() -> new AppException(ErrorCode.STATUS_HISTORY_NOT_FOUND));

            TransactionStatusHistory lastTransactionStatusHistory =
                    order.getTransaction().getTransactionStatusHistories().stream()
                            .max(Comparator.comparing(TransactionStatusHistory::getCreatedAt))
                            .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_STATUS_HISTORY_NOT_FOUND));
            OrderResponseAdmin orderResponseAdmin = orderMapper.toOrderResponseAdmin(order);

            String defaultAddressStr = String.format(
                    "%s%s, %s, %s, %s",
                    order.getDetailLocate() != null ? order.getDetailLocate() + ", " : "",
                    order.getDetailAddress(),
                    order.getSubDistrict(),
                    order.getDistrict(),
                    order.getProvince());
            orderResponseAdmin.setDefaultAddressStr(defaultAddressStr);
            orderResponseAdmin.setCurrentStatus(
                    lastStatusHistory.getOrderStatus().getName());
            orderResponseAdmin.setCurrentStatusTransaction(
                    lastTransactionStatusHistory.getTransactionStatus().getName());

            List<OrderItemResponseAdmin> orderItemResponseAdmins =
                    orderItemMapper.toOrderItemResponseAdmins(order.getOrderItems());
            orderResponseAdmin.setOrderItems(orderItemResponseAdmins);
            orderResponseAdmins.add(orderResponseAdmin);
        }));

        return PaginationResponse.<OrderResponseAdmin>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(orderResponseAdmins)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseAdmin getOneOrderByAdmin(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String defaultAddressStr = String.format(
                "%s%s, %s, %s, %s",
                order.getDetailLocate() != null ? order.getDetailLocate() + ", " : "",
                order.getDetailAddress(),
                order.getDistrict(),
                order.getSubDistrict(),
                order.getProvince());

        OrderResponseAdmin orderResponseAdmin = orderMapper.toOrderResponseAdmin(order);
        orderResponseAdmin.setDefaultAddressStr(defaultAddressStr);
        orderResponseAdmin.setCurrentStatus(
                order.getOrderStatusHistories().getLast().getOrderStatus().getName());
        orderResponseAdmin.setCurrentStatusTransaction(order.getTransaction()
                .getTransactionStatusHistories()
                .getLast()
                .getTransactionStatus()
                .getName());
        List<OrderItemResponseAdmin> orderItemResponseAdmins =
                orderItemMapper.toOrderItemResponseAdmins(order.getOrderItems());
        orderResponseAdmin.setOrderItems(orderItemResponseAdmins);

        return orderResponseAdmin;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateOrderStatusByAdmin(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().getLast();
        OrderStatusName currentStatus =
                OrderStatusName.valueOf(lastStatusHistory.getOrderStatus().getName());
        OrderStatusName nextStatusName = getNextStatusAdmin(currentStatus);
        OrderStatus nextStatus = orderStatusRepository
                .findByName(nextStatusName.name())
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

        order.getOrderStatusHistories()
                .add(OrderStatusHistory.builder()
                        .order(order)
                        .orderStatus(nextStatus)
                        .remarks(order.getOrderStatusHistories().getLast().getRemarks())
                        .build());
        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while updating order status: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void cancelOrderByAdmin(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().getLast();
        if (OrderStatusName.CANCELLED
                .name()
                .equals(lastStatusHistory.getOrderStatus().getName())) {
            throw new AppException(ErrorCode.ORDER_CANCELLED);
        }

        OrderStatus cancelledStatus = orderStatusRepository
                .findByName(OrderStatusName.CANCELLED.name())
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

        OrderStatusHistory cancelledStatusHistory = OrderStatusHistory.builder()
                .order(order)
                .orderStatus(cancelledStatus)
                .remarks(order.getOrderStatusHistories().getLast().getRemarks())
                .build();

        order.getOrderStatusHistories().add(cancelledStatusHistory);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling order: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    private OrderStatusName getNextStatusAdmin(OrderStatusName currentStatus) {
        return switch (currentStatus) {
            case WAITING_FOR_SHIPPING -> OrderStatusName.PICKED_UP;
            case PICKED_UP -> OrderStatusName.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> OrderStatusName.DELIVERED;
            default -> throw new AppException(ErrorCode.SELLER_PREPARING_COMPLETED_ORDER);
        };
    }

    @PreAuthorize("hasRole('USER')")
    public PaginationResponse<OrderResponseUser> getAllOrderByUser(
            String pageStr, String sizeStr, String sortBy, String orderBy, String search, String filter) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        String userId = user.getId();

        if (!Arrays.asList(SORT_BY_USER).contains(sortBy)) sortBy = null;
        if (!Arrays.asList(ORDER_BY).contains(orderBy)) orderBy = null;
        Sort sortable = (sortBy == null || orderBy == null)
                ? Sort.unsorted()
                : Sort.by(Sort.Direction.fromString(orderBy), sortBy);

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        var pageData = orderRepository.findAllOrderByUser(userId, search, search, search, filter, pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);
        List<OrderResponseUser> orderResponseUsers = new ArrayList<>();
        pageData.getContent().forEach((order -> {
            OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().stream()
                    .max(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                    .orElseThrow(() -> new AppException(ErrorCode.STATUS_HISTORY_NOT_FOUND));
            TransactionStatusHistory lastTransactionStatusHistory =
                    order.getTransaction().getTransactionStatusHistories().stream()
                            .max(Comparator.comparing(TransactionStatusHistory::getCreatedAt))
                            .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_STATUS_HISTORY_NOT_FOUND));

            OrderResponseUser orderResponseUser = orderMapper.toOrderResponseUser(order);

            String defaultAddressStr = String.format(
                    "%s%s, %s, %s, %s",
                    order.getDetailLocate() != null ? order.getDetailLocate() + ", " : "",
                    order.getDetailAddress(),
                    order.getSubDistrict(),
                    order.getDistrict(),
                    order.getProvince());
            orderResponseUser.setDefaultAddressStr(defaultAddressStr);
            orderResponseUser.setCurrentStatus(
                    lastStatusHistory.getOrderStatus().getName());
            orderResponseUser.setCurrentStatusTransaction(
                    lastTransactionStatusHistory.getTransactionStatus().getName());

            List<OrderItemResponseUser> orderItemResponseUsers =
                    orderItemMapper.toOrderItemResponseList(order.getOrderItems());
            orderResponseUser.setOrderItems(orderItemResponseUsers);
            orderResponseUsers.add(orderResponseUser);
        }));

        return PaginationResponse.<OrderResponseUser>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(orderResponseUsers)
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    public OrderResponseUser getOneOrderByUser(String orderId) {
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

        OrderResponseUser orderResponseUser = orderMapper.toOrderResponseUser(order);
        orderResponseUser.setDefaultAddressStr(defaultAddressStr);
        orderResponseUser.setCurrentStatus(
                order.getOrderStatusHistories().getLast().getOrderStatus().getName());
        orderResponseUser.setCurrentStatusTransaction(order.getTransaction()
                .getTransactionStatusHistories()
                .getLast()
                .getTransactionStatus()
                .getName());

        List<OrderItemResponseUser> orderItemResponseUsers =
                orderItemMapper.toOrderItemResponseList(order.getOrderItems());
        orderResponseUser.setOrderItems(orderItemResponseUsers);
        List<OrderStatusHistoryResponseUser> orderStatusHistoryResponseUsers =
                orderMapper.toOrderStatusHistoryResponseUserList(order.getOrderStatusHistories());
        orderResponseUser.setOrderStatusHistories(orderStatusHistoryResponseUsers);

        return orderResponseUser;
    }

    @PreAuthorize("hasRole('USER')")
    public void cancelOrderByUser(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_USER);
        }

        OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().getLast();
        if (OrderStatusName.CANCELLED
                .name()
                .equals(lastStatusHistory.getOrderStatus().getName())) {
            throw new AppException(ErrorCode.ORDER_CANCELLED);
        }

        OrderStatus cancelledStatus = orderStatusRepository
                .findByName(OrderStatusName.CANCELLED.name())
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

        OrderStatusHistory cancelledStatusHistory = OrderStatusHistory.builder()
                .order(order)
                .orderStatus(cancelledStatus)
                .remarks(order.getOrderStatusHistories().getLast().getRemarks())
                .build();

        order.getOrderStatusHistories().add(cancelledStatusHistory);

        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while cancelling order: {}", e.getMessage());
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

                if (Boolean.TRUE.equals(hasVariant)) {
                    variant.setQuantity(variant.getQuantity() - orderItemRequest.getQuantity());
                }
                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .price(hasVariant ? variant.getOriginalPrice() : product.getOriginalPrice())
                        .discount(hasVariant ? variant.getOriginalPrice().subtract(variant.getSalePrice()) : product.getOriginalPrice().subtract(product.getSalePrice()))
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
