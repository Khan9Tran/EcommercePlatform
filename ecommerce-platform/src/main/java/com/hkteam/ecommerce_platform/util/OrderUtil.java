package com.hkteam.ecommerce_platform.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.hkteam.ecommerce_platform.dto.response.OrderDetailAdminResponse;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.payment.TransactionStatusHistory;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.product.Value;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.entity.status.OrderStatus;
import com.hkteam.ecommerce_platform.entity.user.Address;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.enums.PaymentMethod;
import com.hkteam.ecommerce_platform.enums.TransactionStatusName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.*;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderUtil {
    public String validateSortOrOrder(String value, String[] validValues) {
        return Arrays.asList(validValues).contains(value) ? value : null;
    }

    public Sort validateSortAndOrder(String sortBy, String orderBy, String[] validSortBy, String[] validOrderBy) {
        sortBy = validateSortOrOrder(sortBy, validSortBy);
        orderBy = validateSortOrOrder(orderBy, validOrderBy);
        return (sortBy == null || orderBy == null)
                ? Sort.unsorted()
                : Sort.by(Sort.Direction.fromString(orderBy), sortBy);
    }

    public String maskPhone(String phone) {
        if (phone != null && phone.length() > 3) {
            return phone.substring(0, phone.length() - 3) + "***";
        }
        return phone;
    }

    public String maskEmail(String email) {
        if (email != null && email.contains("@")) {
            int atIndex = email.indexOf("@");
            if (atIndex > 3) {
                return email.substring(0, atIndex - 3) + "***" + email.substring(atIndex);
            }
        }
        return email;
    }

    public OrderStatusName getNextStatusSeller(OrderStatusName currentStatus) {
        return switch (currentStatus) {
            case PENDING -> OrderStatusName.CONFIRMED;
            case CONFIRMED -> OrderStatusName.PREPARING;
            case PREPARING -> OrderStatusName.WAITING_FOR_SHIPPING;
            default -> null;
        };
    }

    public void addStoreAddress(
            Order order, AddressRepository addressRepository, OrderDetailAdminResponse orderDetailAdminResponse) {
        if (Objects.nonNull(order.getStore().getDefaultAddressId())) {
            Address address = addressRepository
                    .findById(order.getStore().getDefaultAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

            orderDetailAdminResponse.setStoreProvince(address.getProvince());
            orderDetailAdminResponse.setStoreDistrict(address.getDistrict());
            orderDetailAdminResponse.setStoreSubDistrict(address.getSubDistrict());
            orderDetailAdminResponse.setStoreDetailAddress(address.getDetailAddress());
            orderDetailAdminResponse.setStoreDetailLocate(address.getDetailLocate());
        }
    }

    public OrderStatusName getNextStatusAdmin(OrderStatusName currentStatus) {
        return switch (currentStatus) {
            case WAITING_FOR_SHIPPING -> OrderStatusName.PICKED_UP;
            case PICKED_UP -> OrderStatusName.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> OrderStatusName.DELIVERED;
            default -> null;
        };
    }

    public OrderStatusHistory getLastOrderStatusHistory(Order order) {
        return order.getOrderStatusHistories().stream()
                .max(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                .orElseThrow(() -> new AppException(ErrorCode.STATUS_HISTORY_NOT_FOUND));
    }

    public TransactionStatusHistory getLastTransactionStatusHistory(Order order) {
        return order.getTransaction().getTransactionStatusHistories().stream()
                .max(Comparator.comparing(TransactionStatusHistory::getCreatedAt))
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_STATUS_HISTORY_NOT_FOUND));
    }

    public void validateNotCancelled(OrderStatusHistory lastStatusHistory) {
        if (OrderStatusName.CANCELLED
                .name()
                .equals(lastStatusHistory.getOrderStatus().getName())) {
            throw new AppException(ErrorCode.ORDER_CANCELLED);
        }
    }

    public void restoreProductQuantity(
            Order order,
            ProductRepository productRepository,
            ProductElasticsearchRepository productElasticsearchRepository,
            VariantRepository variantRepository) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            var esPro = productElasticsearchRepository
                    .findById(orderItem.getProduct().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            if (Objects.nonNull(product.getVariants()) && !product.getVariants().isEmpty()) {
                Variant variant = product.getVariants().stream()
                        .filter(v -> v.getValues().stream()
                                .map(Value::getValue)
                                .toList()
                                .equals(orderItem.getValues()))
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                variantRepository.updateQuantityById(variant.getQuantity() + orderItem.getQuantity(), variant.getId());
            }
            product.setQuantity(product.getQuantity() + orderItem.getQuantity());
            productRepository.save(product);
            esPro.setQuantity(esPro.getQuantity() + orderItem.getQuantity());
            productElasticsearchRepository.save(esPro);
        }
    }

    public void cancelOneOrder(
            Order order,
            OrderStatusRepository orderStatusRepository,
            ProductRepository productRepository,
            ProductElasticsearchRepository productElasticsearchRepository,
            VariantRepository variantRepository,
            OrderStatusName cancellationStatus) {
        try {
            OrderStatusHistory lastStatusHistory = getLastOrderStatusHistory(order);

            validateNotCancelled(lastStatusHistory);

            OrderStatus cancelledStatus = orderStatusRepository
                    .findByName(cancellationStatus.name())
                    .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND));

            order.getOrderStatusHistories()
                    .add(OrderStatusHistory.builder()
                            .order(order)
                            .orderStatus(cancelledStatus)
                            .remarks(lastStatusHistory.getRemarks())
                            .build());

            restoreProductQuantity(order, productRepository, productElasticsearchRepository, variantRepository);
        } catch (DataIntegrityViolationException e) {
            log.error("Error in function cancelOneOrder at OrderUtil: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    public void updateOneOrderStatusByAdmin(
            Order order,
            OrderStatusRepository orderStatusRepository,
            TransactionStatusRepository transactionStatusRepository,
            ProductRepository productRepository,
            ProductElasticsearchRepository productElasticsearchRepository,
            VariantRepository variantRepository) {
        OrderStatusHistory lastStatusHistory = getLastOrderStatusHistory(order);

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
                        .remarks(lastStatusHistory.getRemarks())
                        .build());

        try {
            if (order.getTransaction().getPayment().getPaymentMethod().name().equals(PaymentMethod.COD.name())
                    && nextStatus.equals(orderStatusRepository
                            .findByName(OrderStatusName.DELIVERED.name())
                            .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND)))) {

                order.getTransaction()
                        .getTransactionStatusHistories()
                        .add(TransactionStatusHistory.builder()
                                .transactionStatus(transactionStatusRepository
                                        .findById(TransactionStatusName.SUCCESS.name())
                                        .orElseThrow(() -> new AppException(ErrorCode.STATUS_NOT_FOUND)))
                                .remarks("Payment COD completed.")
                                .transaction(order.getTransaction())
                                .build());

                for (OrderItem orderItem : order.getOrderItems()) {
                    Product product = orderItem.getProduct();
                    var esPro = productElasticsearchRepository
                            .findById(orderItem.getProduct().getId())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

                    if (Objects.nonNull(product.getVariants())
                            && !product.getVariants().isEmpty()) {
                        Variant variant = product.getVariants().stream()
                                .filter(v -> v.getValues().stream()
                                        .map(Value::getValue)
                                        .toList()
                                        .equals(orderItem.getValues()))
                                .findFirst()
                                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                        variantRepository.updateSoldById(variant.getSold() + orderItem.getQuantity(), variant.getId());
                    }
                    product.setSold(product.getSold() + orderItem.getQuantity());
                    productRepository.save(product);
                    esPro.setSold(esPro.getSold() + orderItem.getQuantity());
                    productElasticsearchRepository.save(esPro);
                }
            }
        } catch (DataIntegrityViolationException e) {
            log.error("Error in function updateOneOrderStatusByAdmin at OrderUtil: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    public void updateOneOrderStatusBySeller(
            AuthenticatedUserUtil authenticatedUserUtil, Order order, OrderStatusRepository orderStatusRepository) {
        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(order))) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_STORE);
        }

        OrderStatusHistory lastStatusHistory = getLastOrderStatusHistory(order);

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
                        .remarks(lastStatusHistory.getRemarks())
                        .build());
    }

    public void setMappingVariantId(OrderItem orderItem, Object orderItemResponse) {
        Product product = orderItem.getProduct();
        if (Objects.nonNull(product)) {
            product.getVariants().forEach(variant -> {
                List<String> listVariantValue =
                        variant.getValues().stream().map(Value::getValue).toList();

                if (listVariantValue.equals(orderItem.getValues())) {
                    try {
                        var setVariantIdMethod = orderItemResponse.getClass().getMethod("setVariantId", String.class);
                        setVariantIdMethod.invoke(orderItemResponse, variant.getId());
                    } catch (Exception e) {
                        log.error("Error in function setMatchingVariantId at OrderUtil: {}", e.getMessage());
                        throw new AppException(ErrorCode.UNKNOWN_ERROR);
                    }
                }
            });
        }
    }
}
