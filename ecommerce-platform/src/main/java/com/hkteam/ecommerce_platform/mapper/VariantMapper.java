package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.dto.response.VariantOfProductUserViewResponse;
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
    @Mapping(target ="isAvailable", source = "available")
    Variant toVariant(VariantOfProductRequest request);

    VariantOfProductResponse toVariantOfProductResponse(Variant variant);

    @Mapping(target = "available", source = "available")
    void updateVariantFromRequest(UpdateVariantRequest request, @MappingTarget Variant variant);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "values", ignore = true)
    VariantDetailResponse toVariantDetailResponse(Variant variant);

    @Mapping(target = "values", ignore = true)
    VariantOfProductUserViewResponse toVariantOfProductUserViewResponse(Variant variant);
}
