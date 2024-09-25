package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;

import com.hkteam.ecommerce_platform.dto.request.ComponentCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ComponentResponse;
import com.hkteam.ecommerce_platform.entity.category.Component;

@Mapper(componentModel = "spring")
public interface ComponentMapper {
    Component toComponent(ComponentCreationRequest request);

    ComponentResponse toComponentResponse(Component component);
}
