package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;

import com.hkteam.ecommerce_platform.dto.response.AttributeOfProductResponse;
import com.hkteam.ecommerce_platform.entity.product.Attribute;

@Mapper(componentModel = "spring")
public interface AttributeMapper {
    AttributeOfProductResponse toAttributeOfProductResponse(Attribute attribute);
}
