package com.hkteam.ecommerce_platform.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "user.username", target = "accountName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phone", target = "userPhone")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt")
    OrderResponseSeller toOrderResponseSeller(Order order);

    @Mapping(source = "user.username", target = "accountName")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phone", target = "userPhone")
    @Mapping(source = "phone", target = "orderPhone")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt")
    OrderResponseAdmin toOrderResponseAdmin(Order order);

    @Mapping(source = "user.username", target = "accountName")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phone", target = "userPhone")
    @Mapping(source = "phone", target = "orderPhone")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "store.rating", target = "ratingStore")
    @Mapping(source = "store.user.imageUrl", target = "avatarStore")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt")
    OrderResponseUser toOrderResponseUser(Order order);

    @Mapping(source = "orderStatus.name", target = "orderStatusName")
    OrderStatusHistoryResponse toOrderStatusHistoryResponse(OrderStatusHistory orderStatusHistory);

    List<OrderStatusHistoryResponse> toOrderStatusHistoryResponseList(List<OrderStatusHistory> orderStatusHistories);

    @Mapping(source = "orderStatus.name", target = "orderStatusName")
    OrderStatusHistoryResponseUser toOrderStatusHistoryResponseUser(OrderStatusHistory orderStatusHistory);

    List<OrderStatusHistoryResponseUser> toOrderStatusHistoryResponseUserList(
            List<OrderStatusHistory> orderStatusHistories);
}
