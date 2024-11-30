package com.hkteam.ecommerce_platform.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.hkteam.ecommerce_platform.dto.response.OrderItemResponseAdmin;
import com.hkteam.ecommerce_platform.dto.response.OrderItemResponseSeller;
import com.hkteam.ecommerce_platform.dto.response.OrderItemResponseUser;
import com.hkteam.ecommerce_platform.dto.response.OrderResponseAdmin;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponseUser toOrderItemResponseUser(OrderItem orderItem);

    List<OrderItemResponseUser> toOrderItemResponseList(List<OrderItem> orderItems);

    OrderItemResponseSeller toOrderItemResponseSeller(OrderItem orderItem);

    List<OrderItemResponseSeller> toOrderItemResponseSellerList(List<OrderItem> orderItems);

    OrderResponseAdmin toOrderResponseAdmin(OrderItem orderItem);

    List<OrderItemResponseAdmin> toOrderItemResponseAdmins(List<OrderItem> orderItems);
}
