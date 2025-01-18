package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.order.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "user.username", target = "userAccountName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phone", target = "userPhone")
    OrderDetailSellerResponse toOrderDetailSellerResponse(Order order);

    OrderGetAllSellerResponse toOrderGetAllSellerResponse(Order order);

    @Mapping(source = "user.username", target = "userAccountName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.phone", target = "userPhone")
    @Mapping(source = "phone", target = "orderPhone")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "store.rating", target = "ratingStore")
    @Mapping(source = "store.user.imageUrl", target = "avatarStore")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "transaction.payment.paymentMethod", target = "paymentMethod")
    @Mapping(source = "store.user.username", target = "storeAccountName")
    @Mapping(source = "store.user.phone", target = "storePhone")
    OrderDetailAdminResponse toOrderDetailAdminResponse(Order order);

    @Mapping(source = "transaction.payment.paymentMethod", target = "paymentMethod")
    OrderGetAllAdminResponse toOrderGetAllAdminResponse(Order order);

    @Mapping(source = "phone", target = "orderPhone")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "store.rating", target = "ratingStore")
    @Mapping(source = "store.user.imageUrl", target = "avatarStore")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "transaction.payment.paymentMethod", target = "paymentMethod")
    OrderGetOneUserResponse toOrderGetOneUserResponse(Order order);

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.user.imageUrl", target = "avatarStore")
    @Mapping(source = "store.name", target = "storeName")
    @Mapping(source = "store.rating", target = "ratingStore")
    @Mapping(source = "transaction.payment.paymentMethod", target = "paymentMethod")
    OrderGetAllUserResponse toOrderGetAllUserResponse(Order order);
}
