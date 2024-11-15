package com.hkteam.ecommerce_platform.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.OrderRequest;
import com.hkteam.ecommerce_platform.dto.response.OrderItemResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.status.OrderStatus;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.OrderMapper;
import com.hkteam.ecommerce_platform.repository.OrderRepository;
import com.hkteam.ecommerce_platform.repository.OrderStatusRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.PageUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {
    OrderRepository orderRepository;
    OrderStatusRepository orderStatusRepository;
    OrderMapper orderMapper;
    AuthenticatedUserUtil authenticatedUserUtil;

    static String[] SORT_BY = {"createdAt"};
    static String[] ORDER_BY = {"asc", "desc"};

    public PaginationResponse<OrderResponse> getAllOrderBySeller(String pageStr, String sizeStr, String sortBy, String orderBy, String search) {
        var user = authenticatedUserUtil.getAuthenticatedUser();
        if (user.getStore() == null) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        String storeId = user.getStore().getId();

        if (!Arrays.asList(SORT_BY).contains(sortBy)) sortBy = null;
        if (!Arrays.asList(ORDER_BY).contains(orderBy)) orderBy = null;
        Sort sortable =
                (sortBy == null || orderBy == null) ? Sort.unsorted() : Sort.by(Sort.Direction.fromString(orderBy), sortBy);

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        var pageData = orderRepository.findAllOrderByStore(storeId, search, search, search, pageable);
        //var pageData = orderRepository.findByStore_IdContainsAndOrderStatusHistories_OrderStatus_NameLikeOrIdLike(storeId, search, search, pageable);

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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(order))) {
            throw new AppException(ErrorCode.ORDER_NOT_BELONG_TO_STORE);
        }

        OrderStatusHistory lastStatusHistory = order.getOrderStatusHistories().getLast();
        if (OrderStatusName.CANCELLED.name().equals(lastStatusHistory.getOrderStatus().getName())) {
            throw new AppException(ErrorCode.ORDER_CANCELLED);
        }

        OrderStatus cancelledStatus = orderStatusRepository.findByName(OrderStatusName.CANCELLED.name())
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

    public OrderResponse createOrder(OrderRequest orderRequest) {
        return null;
    }
}
