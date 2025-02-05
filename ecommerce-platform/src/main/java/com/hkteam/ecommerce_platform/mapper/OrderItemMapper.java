package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.OrderItemGetAllUserResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderItemGetOneAdminResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderItemGetOneSellerResponse;
import com.hkteam.ecommerce_platform.dto.response.OrderItemGetOneUserResponse;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    @Mapping(source = "product.brand.name", target = "productNameBrand")
    @Mapping(source = "product.slug", target = "productSlug")
    OrderItemGetOneSellerResponse toOrderItemGetOneSellerResponse(OrderItem orderItem);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    @Mapping(source = "product.brand.name", target = "productNameBrand")
    @Mapping(source = "product.slug", target = "productSlug")
    OrderItemGetOneAdminResponse toOrderItemGetOneAdminResponse(OrderItem orderItem);

    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    @Mapping(source = "product.slug", target = "productSlug")
    @Mapping(source = "product.id", target = "productId")
    OrderItemGetOneUserResponse toOrderItemGetOneUserResponse(OrderItem orderItem);

    @Mapping(source = "product.slug", target = "productSlug")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImageUrl", target = "productMainImageUrl")
    @Mapping(source = "product.id", target = "productId")
    OrderItemGetAllUserResponse toOrderItemGetAllUserResponse(OrderItem orderItem);
}
