package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hkteam.ecommerce_platform.dto.request.StoreRegistrationRequest;
import com.hkteam.ecommerce_platform.dto.response.StoreDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreOfProductResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreRegistrationResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreResponse;
import com.hkteam.ecommerce_platform.entity.user.Store;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    @Mapping(source = "user.username", target = "username")
    StoreResponse toStoreResponse(Store store);

    @Mapping(source = "user.username", target = "username")
    StoreDetailResponse toStoreDetailResponse(Store store);

    Store toStore(StoreRegistrationRequest request);

    StoreRegistrationResponse toStoreRegistrationResponse(Store store);

    void updateStore(@MappingTarget Store store, StoreRegistrationRequest request);

    StoreOfProductResponse toStoreOfProductResponse(Store store);
}
