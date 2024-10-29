package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.request.UpdateValueRequest;
import com.hkteam.ecommerce_platform.dto.response.ValueDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.ValueOfVariantResponse;
import com.hkteam.ecommerce_platform.entity.product.Value;

@Mapper(componentModel = "spring")
public interface ValueMapper {

    @Mapping(target = "attribute", source = "attribute.name")
    ValueOfVariantResponse toValueOfVariantResponse(Value value);

    Value toValue(UpdateValueRequest request);

    ValueDetailResponse toValueDetailResponse(Value value);
}
