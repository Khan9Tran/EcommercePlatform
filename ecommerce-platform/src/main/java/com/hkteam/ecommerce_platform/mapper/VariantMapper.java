package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.dto.request.VariantOfProductRequest;
import com.hkteam.ecommerce_platform.dto.response.VariantOfProductResponse;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    @Mapping(target = "values", ignore = true)
    Variant toVariant(VariantOfProductRequest request);

    VariantOfProductResponse toVariantOfProductResponse(Variant variant);
}
