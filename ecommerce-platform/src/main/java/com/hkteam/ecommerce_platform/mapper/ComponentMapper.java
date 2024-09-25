package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.request.ComponentCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ComponentResponse;
import com.hkteam.ecommerce_platform.entity.category.Component;

@Mapper(componentModel = "spring")
public interface ComponentMapper {
    Component toComponent(ComponentCreationRequest request);

    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "lastUpdatedAt", target = "lastUpdatedAt")
    ComponentResponse toComponentResponse(Component component);
}
