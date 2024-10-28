package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.ProductDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.VariantOfProductResponse;
import com.hkteam.ecommerce_platform.entity.product.Attribute;
import com.hkteam.ecommerce_platform.entity.product.Value;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.mapper.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.response.ProductCreationResponse;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;
import com.hkteam.ecommerce_platform.enums.TypeSlug;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.*;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;
import com.hkteam.ecommerce_platform.util.SlugUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductService {
    private final CategoryMapper categoryMapper;
    private final ComponentMapper componentMapper;
    ProductMapper productMapper;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductRepository productRepository;
    ComponentRepository componentRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    VariantMapper variantMapper;
    AttributeRepository attributeRepository;
    BrandMapper brandMapper;
    StoreMapper storeMapper;
    ProductComponentValueMapper productComponentValueMapper;

    @PreAuthorize("hasRole('SELLER')")
    public ProductCreationResponse createProduct(ProductCreationRequest request) {

        var product = productMapper.toProduct(request);

        product.setSlug(SlugUtils.getSlug(product.getName(), TypeSlug.PRODUCT));

        var owner = authenticatedUserUtil.getAuthenticatedUser();

        if (Objects.isNull(owner.getStore())) throw new AppException(ErrorCode.STORE_NOT_FOUND);

        product.setStore(owner.getStore());

        var category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        product.setCategory(category);

        var brand = brandRepository
                .findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        product.setBrand(brand);

        var componentRequest = request.getComponents();

        Set<Component> components = new HashSet<>();
        product.setProductComponentValues(new HashSet<>());

        componentRequest.forEach((component) -> {
            var cp = componentRepository
                    .findById(component.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));
            product.getProductComponentValues()
                    .add(ProductComponentValue.builder()
                            .value(component.getValue())
                            .component(cp)
                            .product(product)
                            .build());
            components.add(cp);
        });

        if (!Objects.isNull(category.getComponents()) && components.isEmpty())
            throw new AppException(ErrorCode.COMPONENT_NOT_FOUND);

        if (!category.getComponents().equals(components)) throw new AppException(ErrorCode.COMPONENT_NOT_FOUND);

        List<Attribute> attributes = new ArrayList<Attribute>();

        List<Variant> variants = new ArrayList<>();

        if (!Objects.isNull(request.getAttributesHasValues()) && !Objects.isNull(request.getVariantOfProducts()))
        {
            request.getAttributesHasValues().forEach((attribute) -> {
                Set<Value> values = new HashSet<>();
                var attr = Attribute.builder().name(attribute.getName()).createdBy(owner).build();
                attribute.getValue().forEach((value) -> {
                    values.add(Value.builder().value(value).createdBy(owner).attribute(attr).build());
                });
                attr.setValues(values);
                attributes.add(attr);
            });

            request.getVariantOfProducts().forEach((variant) -> {
                var vr = variantMapper.toVariant(variant);
                vr.setSlug(SlugUtils.getSlug(product.getName(), TypeSlug.VARIANT));
                vr.setValues(getValuesOfVariant(attributes, variant.getValues()));
                vr.setProduct(product);
                variants.add(vr);
            });


            product.setQuantity(countTotalProductQuantity(variants));
            product.setOriginalPrice(findMinOriginalPrice(variants));
            product.setSalePrice(findMinSalePrice(variants));
        }

        product.setVariants(variants);

        product.getVariants().forEach((variant) -> {
            log.info("variant: " + variant.getQuantity( ));
            variant.getValues().forEach(
                    (value) -> {
                        log.info("value: " + value.getValue());
                    }
            );
        });

        try {
        attributes.forEach((attribute) -> {
            attributeRepository.save(attribute);
        });
        productRepository.save(product);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        ProductCreationResponse response = productMapper.toProductCreationResponse(product);
        List<VariantOfProductResponse> variantOfProductResponses = new ArrayList<>();
        product.getVariants().forEach((variant) -> {
            variantOfProductResponses.add(variantMapper.toVariantOfProductResponse(variant));
        });
        response.setVariants(variantOfProductResponses);
        return  response;
    }

    private List<Value> getValuesOfVariant(List<Attribute> attributes, List<String> values) {
        List<Value> valueSet = new ArrayList<>();
        int index = 0;
        for (Attribute attribute : attributes) {
            if (index < values.size()) {
                for (Value value : attribute.getValues()) {
                    if (value.getValue().equals(values.get(index))) {
                        valueSet.add(value);
                    }
                }
            }
            index++;
        }
        return valueSet;
    }

    private BigDecimal findMinSalePrice(List<Variant> variants) {
        return variants.stream().map(Variant::getSalePrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private BigDecimal findMinOriginalPrice(List<Variant> variants) {
        return variants.stream().map(Variant::getOriginalPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private int countTotalProductQuantity(List<Variant> variants) {
        return variants.stream().mapToInt(Variant::getQuantity).sum();
    }

    public ProductDetailResponse updateProduct(String id, ProductUpdateRequest request) {
        var product = productRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product = productMapper.toProduct(request);

        if (Objects.isNull(product.getVariants())) {
            product.setQuantity(request.getQuantity());
            product.setOriginalPrice(request.getOriginalPrice());
            product.setSalePrice(request.getSalePrice());
        }

        try {
            productRepository.save(product);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
        return null;
    }

    public ProductDetailResponse getProductBySlug(String slug) {
        return null;
    }

    public ProductDetailResponse getProduct(String id) {
        var product = productRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        var response = productMapper.toProductDetailResponse(product);
        response.setCategory(categoryMapper.toCategoryOfProductResponse(product.getCategory()));
        response.setBrand(brandMapper.toBrandOfProductResponse(product.getBrand()));
        response.setStore(storeMapper.toStoreOfProductResponse(product.getStore()));
        response.setComponents(product.getProductComponentValues().stream().map(productComponentValueMapper::toProductComponentValueOfProductResponse).collect(Collectors.toSet()));
        return response;
    }

    public void deleteProduct(String id) {

    }
}
