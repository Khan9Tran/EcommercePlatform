package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.elasticsearch.EsProComponentValue;
import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
import com.hkteam.ecommerce_platform.util.PageUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;
import com.hkteam.ecommerce_platform.entity.product.Attribute;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.product.Value;
import com.hkteam.ecommerce_platform.entity.product.Variant;
import com.hkteam.ecommerce_platform.enums.TypeSlug;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.*;
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
    ProductElasticsearchRepository productElasticsearchRepository;

    static String[] SORT_BY = { "name", "originalPrice", "salePrice", "rating", "createdAt" };
    static String[] ORDER = { "asc", "desc" };
    static String[] TAB = { "available", "unAvailable", "blocked" };

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


        if (!Objects.isNull(componentRequest) && !componentRequest.isEmpty())
            componentRequest.forEach((component) -> {
                var cp = componentRepository
                        .findById(component.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.COMPONENT_NOT_FOUND));

                if (cp.isRequired() && (Objects.isNull(component.getValue()) || component.getValue().isEmpty()))
                    throw new AppException(ErrorCode.COMPONENT_VALUE_REQUIRED);

                product.getProductComponentValues()
                        .add(ProductComponentValue.builder()
                                .value(component.getValue())
                                .component(cp)
                                .product(product)
                                .build());
                components.add(cp);
            });


        if (!category.getComponents().isEmpty() && components.isEmpty())
            throw new AppException(ErrorCode.COMPONENT_NOT_FOUND);

        //if (!category.getComponents().equals(components)) throw new AppException(ErrorCode.COMPONENT_NOT_FOUND);

        List<Attribute> attributes = new ArrayList<>();

        List<Variant> variants = new ArrayList<>();

        if (!Objects.isNull(request.getAttributesHasValues()) && !Objects.isNull(request.getVariantOfProducts())
                && !request.getAttributesHasValues().isEmpty() && !request.getVariantOfProducts().isEmpty()
        ) {
            request.getAttributesHasValues().forEach((attribute) -> {
                Set<Value> values = new HashSet<>();
                var attr = Attribute.builder()
                        .name(attribute.getName())
                        .createdBy(owner)
                        .build();
                attribute.getValue().forEach((value) -> values.add(Value.builder()
                        .value(value)
                        .createdBy(owner)
                        .attribute(attr)
                        .build()));
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
            product.setOriginalPrice(findMaxOriginalPrice(variants));
            product.setSalePrice(findMinSalePrice(variants));
        }

        product.setVariants(variants);

        product.getVariants().forEach((variant) -> {
            log.info("variant: {}", variant.getQuantity());
            variant.getValues().forEach((value) -> log.info("value: {}", value.getValue()));
        });

        try {
            attributes.forEach(attributeRepository::save);
            productRepository.save(product);
            var productElasticsearch = ProductElasticsearch.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .slug(product.getSlug())
                    .description(product.getDescription())
                    .details(product.getDetails())
                    .originalPrice(product.getOriginalPrice())
                    .salePrice(product.getSalePrice())
                    .isAvailable(product.isAvailable())
                    .quantity(product.getQuantity())
                    .rating(product.getRating())
                    .brandName(product.getBrand().getName())
                    .brandId(product.getBrand().getId())
                    .categoryName(product.getCategory().getName())
                    .categoryId(product.getCategory().getId())
                    .storeName(product.getStore().getName())
                    .storeId(product.getStore().getId())
                    .createdAt(product.getCreatedAt())
                    .lastUpdatedAt(product.getLastUpdatedAt())
                    .isBlocked(product.isBlocked())
                    .productComponentValues(
                            product.getProductComponentValues().stream()
                                    .map(pc -> new EsProComponentValue(pc.getId(), pc.getValue()))
                                    .collect(Collectors.toList())
                    )

                    .build();
            productElasticsearchRepository.save(productElasticsearch);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        ProductCreationResponse response = productMapper.toProductCreationResponse(product);
        List<VariantOfProductResponse> variantOfProductResponses = new ArrayList<>();
        product.getVariants().forEach((variant) -> variantOfProductResponses.add(variantMapper.toVariantOfProductResponse(variant)));
        response.setVariants(variantOfProductResponses);
        return response;
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

    public BigDecimal findMinSalePrice(List<Variant> variants) {
        return variants.stream()
                .map(Variant::getSalePrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal findMaxOriginalPrice(List<Variant> variants) {
        return variants.stream()
                .map(Variant::getOriginalPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private int countTotalProductQuantity(List<Variant> variants) {
        return variants.stream().mapToInt(Variant::getQuantity).sum();
    }

    @PreAuthorize("hasRole('SELLER')")
    public ProductDetailResponse updateProduct(String id, ProductUpdateRequest request) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        var esPro = productElasticsearchRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Long brandId = esPro.getBrandId();

        if (!authenticatedUserUtil.isOwner(product)) throw new AppException(ErrorCode.UNAUTHORIZED);

        productMapper.updateProductFromRequest(request, product);
        productMapper.updateProductFromRequest(request, esPro);

        if (brandId!= null && !Objects.equals(product.getBrand().getId(), brandId)) {
            var brand = brandRepository.findById(request.getBrandId()).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
            product.setBrand(brand);
            esPro.setBrandName(brand.getName());
        }

        if (Objects.isNull(product.getVariants())) {
            product.setQuantity(request.getQuantity());
            product.setOriginalPrice(request.getOriginalPrice());
            product.setSalePrice(request.getSalePrice());

            esPro.setDescription(product.getDescription());
            esPro.setDetails(product.getDetails());
            esPro.setOriginalPrice(product.getOriginalPrice());
        }


        try {

            productRepository.save(product);
            productElasticsearchRepository.save(esPro);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
        return map(product);
    }

    public ProductDetailResponse getProductBySlug(String slug) {
        var product =
                productRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return map(product);
    }

    public ProductDetailResponse getProduct(String id) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return map(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    public void deleteProduct(String id) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!authenticatedUserUtil.isOwner(product)) throw new AppException(ErrorCode.UNAUTHORIZED);

        try {
            product.setDeleted(true);
            productRepository.save(product);

            productElasticsearchRepository.deleteById(product.getId());
        } catch (Exception e) {
            log.info("Has error when delete pro: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    private ProductDetailResponse map(Product product) {
        var response = productMapper.toProductDetailResponse(product);

        response.setCategory(categoryMapper.toCategoryOfProductResponse(product.getCategory()));
        response.setBrand(brandMapper.toBrandOfProductResponse(product.getBrand()));
        response.setStore(storeMapper.toStoreOfProductResponse(product.getStore()));
        response.setComponents(product.getProductComponentValues().stream()
                .map(productComponentValueMapper::toProductComponentValueOfProductResponse)
                .collect(Collectors.toSet()));

        return response;
    }

    @PreAuthorize("hasRole('SELLER')")
    public PaginationResponse<ProductResponse> getAllProducts(String sortBy, String order, String tab, String page,
                                                              String size, String search) {
        if (!Arrays.asList(SORT_BY).contains(sortBy))
            sortBy = null;
        if (!Arrays.asList(ORDER).contains(order))
            order = null;

        if (!Arrays.asList(TAB).contains(tab)) throw new AppException(ErrorCode.TAB_INVALID);

        Sort sortable = (sortBy == null || order == null) ? Sort.unsorted() :  Sort.by(Sort.Direction.fromString(order), sortBy);

        Pageable pageable = PageUtils.createPageable(page, size, sortable);

        var store = authenticatedUserUtil.getAuthenticatedUser().getStore();
        if (Objects.isNull(store)) throw new AppException(ErrorCode.STORE_NOT_FOUND);

        boolean isAvailable = tab.equals("available");
        boolean isBlocked = tab.equals("blocked");
        if (isBlocked) isAvailable = true;

        var pageData = productRepository.findByIsAvailableAndIsBlockedAndStore_IdAndNameContainsIgnoreCase(isAvailable, isBlocked, store.getId(), search, pageable);
        int pageInt = Integer.parseInt(page);

        return PaginationResponse.<ProductResponse>builder()
                .currentPage(pageInt)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? pageInt + 1 : null)
                .previousPage(pageData.hasPrevious() ? pageInt - 1 : null)
                .data(pageData.getContent().stream()
                        .map(productMapper::toProductResponse)
                        .toList())
                .build();
    }

    @PreAuthorize("hasRole('SELLER')")
    public Void updateProductStatus(String id) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!authenticatedUserUtil.isOwner(product)) throw new AppException(ErrorCode.UNAUTHORIZED);
        product.setAvailable(!product.isAvailable());

        try {
            productRepository.save(product);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return null;
    }
}
