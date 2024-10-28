package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ProductDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ProductCreationResponse;
import com.hkteam.ecommerce_platform.entity.product.Product;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "available", target = "isAvailable")
    Product toProduct(ProductCreationRequest request);

    @Mapping(target = "variants", source = "variants")
    ProductCreationResponse toProductCreationResponse(Product product);

    @Mapping(source = "available", target = "available")
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "salePrice", ignore = true)
    void updateProductFromRequest(ProductUpdateRequest request, @MappingTarget Product product);

    @Mapping(source = "available", target = "isAvailable")
    ProductDetailResponse toProductDetailResponse(Product product);
}
