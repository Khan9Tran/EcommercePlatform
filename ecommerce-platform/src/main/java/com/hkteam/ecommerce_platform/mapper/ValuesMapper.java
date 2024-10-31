package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hkteam.ecommerce_platform.dto.request.UpdateValueRequest;
import com.hkteam.ecommerce_platform.dto.response.ValueDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.ValueOfVariantResponse;
import com.hkteam.ecommerce_platform.entity.product.Value;

@Mapper(componentModel = "spring")
public interface ValuesMapper {

    @Mapping(target = "attribute", source = "attribute.name")
    ValueOfVariantResponse toValueOfVariantResponse(Value value);

    void updateValueFromRequest(UpdateValueRequest request, @MappingTarget Value value);

    ValueDetailResponse toValueDetailResponse(Value value);
}
