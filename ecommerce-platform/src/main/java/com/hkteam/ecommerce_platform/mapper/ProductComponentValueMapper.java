package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.entity.elasticsearch.EsProComponentValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.response.ProductComponentValueOfProductResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductComponentValueResponse;
import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;

@Mapper(componentModel = "spring")
public interface ProductComponentValueMapper {
    @Mapping(source = "component.name", target = "name")
    @Mapping(source = "id", target = "valueId")
    @Mapping(source = "component.required", target = "required")
    ProductComponentValueOfProductResponse toProductComponentValueOfProductResponse(
            ProductComponentValue productComponentValue);

    ProductComponentValueResponse toProductComponentValueResponse(ProductComponentValue productComponentValue);
}
