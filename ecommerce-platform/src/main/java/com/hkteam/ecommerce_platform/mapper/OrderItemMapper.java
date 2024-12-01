package com.hkteam.ecommerce_platform.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.OrderItemResponseAdmin;
import com.hkteam.ecommerce_platform.dto.response.OrderItemResponseSeller;
import com.hkteam.ecommerce_platform.dto.response.OrderItemResponseUser;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    @Mapping(source = "product.slug", target = "productSlug")
    OrderItemResponseUser toOrderItemResponseUser(OrderItem orderItem);

    List<OrderItemResponseUser> toOrderItemResponseList(List<OrderItem> orderItems);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    @Mapping(source = "product.brand.name", target = "productNameBrand")
    @Mapping(source = "product.slug", target = "productSlug")
    OrderItemResponseSeller toOrderItemResponseSeller(OrderItem orderItem);

    List<OrderItemResponseSeller> toOrderItemResponseSellerList(List<OrderItem> orderItems);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    @Mapping(source = "product.brand.name", target = "productNameBrand")
    @Mapping(source = "product.slug", target = "productSlug")
    OrderItemResponseAdmin toOrderItemResponseAdmin(OrderItem orderItem);

    List<OrderItemResponseAdmin> toOrderItemResponseAdmins(List<OrderItem> orderItems);
}
