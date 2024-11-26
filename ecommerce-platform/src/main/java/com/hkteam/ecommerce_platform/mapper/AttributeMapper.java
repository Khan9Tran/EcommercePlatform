package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.dto.response.AttributeOfProductResponse;
import com.hkteam.ecommerce_platform.entity.product.Attribute;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttributeMapper {
    AttributeOfProductResponse toAttributeOfProductResponse(Attribute attribute);
}
