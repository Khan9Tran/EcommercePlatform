package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.CartResponse;
import com.hkteam.ecommerce_platform.entity.cart.Cart;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "storeSlug", source = "store.slug")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "avatarStore", source = "store.user.imageUrl")
    @Mapping(target = "ratingStore", source = "store.rating")
    CartResponse toCartResponse(Cart cart);
}
