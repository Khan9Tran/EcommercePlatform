package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.hkteam.ecommerce_platform.dto.response.OrderCreationResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderItemResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.payment.Payment;
import com.hkteam.ecommerce_platform.entity.payment.Transaction;
import com.hkteam.ecommerce_platform.entity.payment.TransactionStatusHistory;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.entity.status.OrderStatus;
import com.hkteam.ecommerce_platform.entity.status.TransactionStatus;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.enums.PaymentMethod;
import com.hkteam.ecommerce_platform.enums.TransactionStatusName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.OrderMapper;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.repository.OrderRepository;
import com.hkteam.ecommerce_platform.repository.OrderStatusRepository;
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
    private final TransactionStatusRepository transactionStatusRepository;
    PaymentRepository paymentRepository;
    StoreRepository storeRepository;
    OrderRepository orderRepository;
    OrderStatusRepository orderStatusRepository;
    OrderMapper orderMapper;
    PaymentService paymentService;
    AuthenticatedUserUtil authenticatedUserUtil;
    ProductRepository productRepository;
    VariantRepository variantRepository;
    AddressRepository addressRepository;

    static String[] SORT_BY = {"createdAt"};
    static String[] ORDER_BY = {"asc", "desc"};

    public PaginationResponse<OrderResponse> getAllOrderBySeller(
            String pageStr, String sizeStr, String sortBy, String orderBy, String search) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (user.getStore() == null) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        String storeId = user.getStore().getId();

        if (!Arrays.asList(SORT_BY).contains(sortBy)) sortBy = null;
        if (!Arrays.asList(ORDER_BY).contains(orderBy)) orderBy = null;
        Sort sortable = (sortBy == null || orderBy == null)
                ? Sort.unsorted()
                : Sort.by(Sort.Direction.fromString(orderBy), sortBy);

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        var pageData = orderRepository.findAllOrderByStore(storeId, search, search, search, pageable);
        // var pageData =
        // orderRepository.findByStore_IdContainsAndOrderStatusHistories_OrderStatus_NameLikeOrIdLike(storeId, search,
        // search, pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);
        List<OrderResponse> orderResponses = new ArrayList<>();
        pageData.getContent().forEach((order -> {
            OrderResponse orderResponse = orderMapper.toOrderResponse(order);

            String defaultAddressStr = String.format(
                    "%s, %s, %s, %s, %s",
                    order.getDetailLocate(),
                    order.getDetailAddress(),
                    order.getDistrict(),
                    order.getSubDistrict(),
                    order.getProvince());
            orderResponse.setDefaultAddressStr(defaultAddressStr);
            orderResponse.setCurrentStatus(
                    order.getOrderStatusHistories().getLast().getOrderStatus().getName());
            orderResponse.setUserPhone(maskPhone(orderResponse.getUserPhone()));
            orderResponse.setPhone(maskPhone(orderResponse.getPhone()));
            orderResponse.setUserEmail(maskEmail(orderResponse.getUserEmail()));

            List<OrderItemResponse> orderItemResponses = orderMapper.toOrderItemResponseList(order.getOrderItems());
            orderResponse.setOrderItems(orderItemResponses);
            orderResponses.add(orderResponse);
        }));

        return PaginationResponse.<OrderResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(orderResponses)
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

    public OrderResponse getOneOrderById(String orderId) {
        Order order =
                orderRepository.findOrderById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String defaultAddressStr = String.format(
                "%s, %s, %s, %s, %s",
                order.getDetailLocate(),
                order.getDetailAddress(),
                order.getDistrict(),
                order.getSubDistrict(),
                order.getProvince());

        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        orderResponse.setDefaultAddressStr(defaultAddressStr);
        orderResponse.setCurrentStatus(
                order.getOrderStatusHistories().getLast().getOrderStatus().getName());
        orderResponse.setUserPhone(maskPhone(orderResponse.getUserPhone()));
        orderResponse.setPhone(maskPhone(orderResponse.getPhone()));
        orderResponse.setUserEmail(maskEmail(orderResponse.getUserEmail()));

        return orderResponse;
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
        log.info("Current order status: {}", currentStatus);
        OrderStatusName nextStatusName = getNextStatus(currentStatus);
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

    private OrderStatusName getNextStatus(OrderStatusName currentStatus) {
        return switch (currentStatus) {
            case ON_HOLD -> OrderStatusName.PENDING;
            case PENDING -> OrderStatusName.CONFIRMED;
            case CONFIRMED -> OrderStatusName.PREPARING;
            case PREPARING -> OrderStatusName.WAITING_FOR_SHIPPING;
            case WAITING_FOR_SHIPPING -> OrderStatusName.PICKED_UP;
            case PICKED_UP -> OrderStatusName.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> OrderStatusName.DELIVERED;
            default -> throw new AppException(ErrorCode.COMPLETED_ORDER);
        };
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 10, backoff = @Backoff(delay = 100))
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

                if (!product.getStore().equals(store)) throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

                if (product.getQuantity() < orderItemRequest.getQuantity())
                    throw new AppException(ErrorCode.QUANTITY_NOT_ENOUGH);

                if (product.isBlocked() || Boolean.FALSE.equals(product.isAvailable()))
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);

                Variant variant = null;
                Boolean hasVariant = Objects.isNull(product.getVariants());

                if (Objects.isNull(orderItemRequest.getVariantId())) {
                    if (hasVariant) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);

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

                if (hasVariant) {
                    variant.setQuantity(variant.getQuantity() - orderItemRequest.getQuantity());
                }
                OrderItem orderItem = OrderItem.builder()
                        .product(product)
                        .price(totalOriginalPrice)
                        .discount(totalOriginalPrice.subtract(totalSalePrice))
                        .quantity(orderItemRequest.getQuantity())
                        .values(
                                !hasVariant
                                        ? null
                                        : variant.getValues().stream()
                                                .map(value -> value.getValue())
                                                .toList())
                        .build();
                orderItems.add(orderItem);
            }

            if (Boolean.FALSE.equals(totalOriginalPrice.equals(verifyTotalOriginalPrice))
                    || Boolean.FALSE.equals(totalSalePrice.equals(verifyTotalSalePrice)))
                throw new AppException(ErrorCode.UNKNOWN_ERROR);

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
                    .total(total)
                    .discount(discount)
                    .phone(address.getPhone())
                    .recipientName(address.getRecipientName())
                    .district(address.getDistrict())
                    .subDistrict(address.getSubDistrict())
                    .detailAddress(address.getDetailAddress())
                    .shippingFee(shippingFee)
                    .detailLocate(address.getDetailLocate())
                    .province(address.getProvince())
                    .note(listOrder.getNote())
                    .grandTotal(amount)
                    .promo(discount)
                    .shippingDiscount(BigDecimal.ZERO)
                    .orderItems(orderItems)
                    .shippingTotal(shippingFee)
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
                    .remarks("Process payment via the VnPay gateway.")
                    .build();

            Transaction transaction = Transaction.builder()
                    .payment(payment)
                    .transactionStatusHistories(new HashSet<>(List.of(transactionStatusHistory)))
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

        return OrderCreationResponse.builder()
                .paymentUrl(
                        isVnPay
                                ? paymentService.createVnPayPayment(amount, request, payment.getId())
                                : "Please pay upon receiving the goods.")
                .status("Success")
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
}