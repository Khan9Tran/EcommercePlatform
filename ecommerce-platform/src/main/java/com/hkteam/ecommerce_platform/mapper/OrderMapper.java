package com.hkteam.ecommerce_platform.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.OrderItemResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderResponseAdmin;
import com.hkteam.ecommerce_platform.dto.response.OrderResponseSeller;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "user.username", target = "accountName")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phone", target = "userPhone")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt")
    OrderResponseSeller toOrderResponseSeller(Order order);

    @Mapping(source = "user.username", target = "accountName")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phone", target = "userPhone")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt")
    OrderResponseAdmin toOrderResponseAdmin(Order order);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);
}
