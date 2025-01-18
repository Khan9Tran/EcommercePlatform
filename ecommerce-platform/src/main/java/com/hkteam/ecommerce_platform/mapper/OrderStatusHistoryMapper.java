package com.hkteam.ecommerce_platform.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.OrderStatusHistoryGetOneUserResponse;
import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;

@Mapper(componentModel = "spring")
public interface OrderStatusHistoryMapper {
    @Mapping(source = "orderStatus.name", target = "orderStatusName")
    OrderStatusHistoryGetOneUserResponse toOrderStatusHistoryGetOneUserResponse(OrderStatusHistory orderStatusHistory);

    List<OrderStatusHistoryGetOneUserResponse> toListOrderStatusHistoryGetOneUserResponse(
            List<OrderStatusHistory> orderStatusHistories);
}
