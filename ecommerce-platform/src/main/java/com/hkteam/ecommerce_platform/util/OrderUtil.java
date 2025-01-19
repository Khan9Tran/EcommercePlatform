package com.hkteam.ecommerce_platform.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

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
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderUtil {
    AddressRepository addressRepository;

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
            default -> throw new AppException(ErrorCode.NOT_PERMISSION_ORDER);
        };
    }

    public void addStoreAddress(Order order, OrderDetailAdminResponse orderDetailAdminResponse) {
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
            default -> throw new AppException(ErrorCode.SELLER_PREPARING_COMPLETED_ORDER);
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

    public void restoreProductQuantities(
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

    public void processOrderCancellation(
            Order order,
            OrderStatusRepository orderStatusRepository,
            ProductRepository productRepository,
            ProductElasticsearchRepository productElasticsearchRepository,
            VariantRepository variantRepository,
            OrderStatusName cancellationStatus) {

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

        restoreProductQuantities(order, productRepository, productElasticsearchRepository, variantRepository);
    }
}
