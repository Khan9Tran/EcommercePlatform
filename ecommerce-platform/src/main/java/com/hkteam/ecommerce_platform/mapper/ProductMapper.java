package com.hkteam.ecommerce_platform.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ProductCreationResponse;
import com.hkteam.ecommerce_platform.entity.product.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "available", target = "isAvailable")
    Product toProduct(ProductCreationRequest request);

    @Mapping(target = "variants", source = "variants")
    ProductCreationResponse toProductCreationResponse(Product product);
}
