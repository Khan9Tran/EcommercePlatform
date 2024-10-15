package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.StoreDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreResponse;
import com.hkteam.ecommerce_platform.entity.user.Store;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    @Mapping(source = "user.username", target = "username")
    StoreResponse toStoreResponse(Store store);

    @Mapping(source = "user.username", target = "username")
    StoreDetailResponse toStoreDetailResponse(Store store);
}
