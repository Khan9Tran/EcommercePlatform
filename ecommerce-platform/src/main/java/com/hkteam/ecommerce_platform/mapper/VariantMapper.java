package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hkteam.ecommerce_platform.dto.request.UpdateVariantRequest;
import com.hkteam.ecommerce_platform.dto.request.VariantOfProductRequest;
import com.hkteam.ecommerce_platform.dto.response.VariantDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.VariantOfProductResponse;
import com.hkteam.ecommerce_platform.entity.product.Variant;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    @Mapping(target = "values", ignore = true)
    Variant toVariant(VariantOfProductRequest request);

    VariantOfProductResponse toVariantOfProductResponse(Variant variant);

    void updateVariantFromRequest(UpdateVariantRequest request, @MappingTarget Variant variant);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "values", ignore = true)
    VariantDetailResponse toVariantDetailResponse(Variant variant);
}
