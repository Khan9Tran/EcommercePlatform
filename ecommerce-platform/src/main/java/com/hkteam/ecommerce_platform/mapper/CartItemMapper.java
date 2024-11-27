package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.CartItemDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.CartItemResponse;
import com.hkteam.ecommerce_platform.entity.cart.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "originalPrice", source = "product.originalPrice")
    @Mapping(target = "salePrice", source = "product.salePrice")
    @Mapping(target = "image", source = "product.mainImageUrl")
    @Mapping(target = "brand", source = "product.brand.name")
    @Mapping(target = "logoBrand", source = "product.brand.logoUrl")
    CartItemDetailResponse toCartItemDetailResponse(CartItem cartItem);
}
