package com.hkteam.ecommerce_platform.mapper;

import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;
import com.hkteam.ecommerce_platform.entity.elasticsearch.EsProComponentValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
import com.hkteam.ecommerce_platform.entity.product.Product;

import java.util.List;
import java.util.Set;

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

    @Mapping(source = "available", target = "available")
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "salePrice", ignore = true)
    void updateProductFromRequest(ProductUpdateRequest request, @MappingTarget ProductElasticsearch product);

    @Mapping(source = "available", target = "isAvailable")
    ProductDetailResponse toProductDetailResponse(Product product);

    @Mapping(source = "available", target = "isAvailable")
    @Mapping(target = "variants", ignore = true)
    ProductUserViewResponse toProductUserViewResponse(Product product);

    ProductOfVariantResponse toProductOfVariantResponse(Product product);

    ProductResponse toProductResponse(ProductElasticsearch productElasticsearch);
    @Mapping(source = "value", target = "value")
    @Mapping(source = "id", target = "valueId")
    ProductComponentValueOfProductResponse toProductComponentValueOfProductResponse(EsProComponentValue productComponentValue);
    Set<ProductComponentValueOfProductResponse> toProductComponentValueOfProductResponseList(List<EsProComponentValue> productComponentValue);

    @Mapping(source = "sold", target = "sold")
    ProductResponse toProductResponse(Product product);
    @Mapping(source = "value", target = "value")
    @Mapping(source = "id", target = "valueId")
    ProductComponentValueOfProductResponse toProductComponentValueOfProductResponse(ProductComponentValue productComponentValue);
    Set<ProductComponentValueOfProductResponse> toProductComponentValueOfProductResponseSet(Set<ProductComponentValue> productComponentValue);


    ProductBestSellingResponse toProductBestSellingResponse(Product product);

    ProductBestInteractionResponse toProductBestInteractionResponse(Product product);
}
