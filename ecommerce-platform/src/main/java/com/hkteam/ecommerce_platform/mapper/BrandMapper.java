package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.hkteam.ecommerce_platform.dto.request.BrandCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.BrandUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.product.Brand;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    Brand toBrand(BrandCreationRequest request);

    BrandResponse toBrandResponse(Brand brand);

    BrandCreationResponse toBrandCreationResponse(Brand brand);

    BrandUpdateResponse toBrandUpdateResponse(Brand brand);

    void updateBrandFromRequest(BrandUpdateRequest request, @MappingTarget Brand brand);

    BrandGetAllResponse toBrandGetAllResponse(Brand brand);

    BrandOfProductResponse toBrandOfProductResponse(Brand brand);
}
