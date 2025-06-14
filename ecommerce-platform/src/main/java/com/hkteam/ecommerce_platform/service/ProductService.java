package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.dto.request.ProductCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.ProductUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.category.Component;
import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;
import com.hkteam.ecommerce_platform.entity.elasticsearch.EsProComponentValue;
import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
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
import com.hkteam.ecommerce_platform.util.PageUtils;
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
    StoreRepository storeRepository;
    CategoryMapper categoryMapper;
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
    ValuesMapper valuesMapper;
    ProductComponentValueMapper productComponentValueMapper;
    ProductElasticsearchRepository productElasticsearchRepository;
    ImageMapper imageMapper;
    AttributeMapper attributeMapper;
    CacheManager cacheManager;

    static final String[] SORT_BY = {"name", "originalPrice", "salePrice", "rating", "createdAt"};
    static final String[] ORDER = {"asc", "desc"};
    static final String[] TAB = {"available", "unAvailable", "blocked"};

    @PreAuthorize("hasRole('SELLER')")
    public ProductCreationResponse createProduct(ProductCreationRequest request) {
        var product = productMapper.toProduct(request);
        if (request.getSalePrice().compareTo(request.getOriginalPrice()) > 0) {
            throw new AppException(ErrorCode.SALE_CANT_GREATER_THAN_ORIGINAL_PRICE);
        }
        product.setSlug(SlugUtils.getSlug(product.getName(), TypeSlug.PRODUCT));

        var owner = authenticatedUserUtil.getAuthenticatedUser();

        if (Objects.isNull(owner.getStore())) throw new AppException(ErrorCode.STORE_NOT_FOUND);

        if (Boolean.TRUE.equals(owner.getStore().isBanned())) throw new AppException(ErrorCode.STORE_BANNED);
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

                if (cp.isRequired()
                        && (Objects.isNull(component.getValue())
                                || component.getValue().isEmpty()))
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

        List<Attribute> attributes = new ArrayList<>();

        List<Variant> variants = new ArrayList<>();

        if (!Objects.isNull(request.getAttributesHasValues())
                && !Objects.isNull(request.getVariantOfProducts())
                && !request.getAttributesHasValues().isEmpty()
                && !request.getVariantOfProducts().isEmpty()) {
            request.getAttributesHasValues().forEach((attribute) -> {
                Set<Value> values = new HashSet<>();
                var attr = Attribute.builder()
                        .name(attribute.getName())
                        .createdBy(owner)
                        .build();
                attribute
                        .getValue()
                        .forEach((value) -> values.add(Value.builder()
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
            attributeRepository.saveAll(attributes);
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
                    .productComponentValues(product.getProductComponentValues().stream()
                            .map(pc -> new EsProComponentValue(pc.getId(), pc.getValue()))
                            .toList())
                    .build();
            productElasticsearchRepository.save(productElasticsearch);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        ProductCreationResponse response = productMapper.toProductCreationResponse(product);
        List<VariantOfProductResponse> variantOfProductResponses = new ArrayList<>();
        product.getVariants()
                .forEach((variant) -> variantOfProductResponses.add(variantMapper.toVariantOfProductResponse(variant)));
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
    public ProductUserViewResponse updateProduct(String id, ProductUpdateRequest request) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        var esPro = productElasticsearchRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Long brandId = esPro.getBrandId();

        if (request.getSalePrice().compareTo(request.getOriginalPrice()) > 0) {
            throw new AppException(ErrorCode.SALE_CANT_GREATER_THAN_ORIGINAL_PRICE);
        }

        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(product)))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        productMapper.updateProductFromRequest(request, product);
        productMapper.updateProductFromRequest(request, esPro);

        if (brandId != null && !Objects.equals(product.getBrand().getId(), brandId)) {
            var brand = brandRepository
                    .findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
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
            evictCache(product);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
        return map(product);
    }

    public ProductUserViewResponse getProductBySlug(String slug) {
        var product =
                productRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.isBlocked() || Boolean.FALSE.equals(product.isAvailable())) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return map(product);
    }

    public ProductUserViewResponse getProduct(String id) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        return map(product);
    }

    @PreAuthorize("hasRole('SELLER')")
    public void deleteProduct(String id) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(product)))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        try {
            product.setDeleted(true);
            productRepository.save(product);
            productElasticsearchRepository.deleteById(product.getId());
            evictCache(product);
        } catch (Exception e) {
            log.info("Has error when delete pro: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    private ProductUserViewResponse map(Product product) {
        var response = productMapper.toProductUserViewResponse(product);
        response.setImages(
                product.getImages().stream().map(imageMapper::toImageInList).collect(Collectors.toSet()));
        Set<Attribute> attributes = new HashSet<>();
        Set<VariantOfProductUserViewResponse> variants = new HashSet<>();
        product.getVariants().forEach((variant) -> {
            if (variant.isAvailable()) {
                var vrMap = variantMapper.toVariantOfProductUserViewResponse(variant);
                List<ValueOfVariantResponse> values = new ArrayList<>();

                variant.getValues().forEach((value) -> {
                    attributes.add(value.getAttribute());
                    values.add(valuesMapper.toValueOfVariantResponse(value));
                });
                vrMap.setValues(values);

                variants.add(vrMap);
            }
        });
        response.setVariants(variants);

        List<AttributeOfProductResponse> attributeOfProductResponses = new ArrayList<>();

        attributes.forEach((attribute) -> {
            var att = attributeMapper.toAttributeOfProductResponse(attribute);
            att.setValues(attribute.getValues().stream()
                    .map(valuesMapper::toValueOfAttributeResponse)
                    .toList());
            attributeOfProductResponses.add(att);
        });

        response.setAttributes(attributeOfProductResponses);

        response.setCategory(categoryMapper.toCategoryOfProductResponse(product.getCategory()));
        response.setBrand(brandMapper.toBrandOfProductResponse(product.getBrand()));
        response.setStore(storeMapper.toStoreOfProductResponse(product.getStore()));
        response.setComponents(product.getProductComponentValues().stream()
                .map(productComponentValueMapper::toProductComponentValueOfProductResponse)
                .collect(Collectors.toSet()));

        return response;
    }

    @PreAuthorize("hasRole('SELLER')")
    public PaginationResponse<ProductResponse> getAllProducts(
            String sortBy, String order, String tab, String page, String size, String search) {
        if (!Arrays.asList(SORT_BY).contains(sortBy)) sortBy = null;
        if (!Arrays.asList(ORDER).contains(order)) order = null;

        if (!Arrays.asList(TAB).contains(tab)) throw new AppException(ErrorCode.TAB_INVALID);

        Sort sortable =
                (sortBy == null || order == null) ? Sort.unsorted() : Sort.by(Sort.Direction.fromString(order), sortBy);

        Pageable pageable = PageUtils.createPageable(page, size, sortable);

        var store = authenticatedUserUtil.getAuthenticatedUser().getStore();
        if (Objects.isNull(store)) throw new AppException(ErrorCode.STORE_NOT_FOUND);

        boolean isAvailable = tab.equals("available");
        boolean isBlocked = tab.equals("blocked");
        if (isBlocked) isAvailable = true;

        var pageData = productRepository.findByIsAvailableAndIsBlockedAndStore_IdAndNameContainsIgnoreCase(
                isAvailable, isBlocked, store.getId(), search, pageable);
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
    @Transactional
    public Void updateProductStatus(String id) {
        var product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (Boolean.FALSE.equals(authenticatedUserUtil.isOwner(product)))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        product.setAvailable(!product.isAvailable());

        try {
            var productElasticsearch = productElasticsearchRepository
                    .findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            productElasticsearch.setAvailable(product.isAvailable());
            productRepository.save(product);
            productElasticsearchRepository.save(productElasticsearch);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
        evictCache(product);
        return null;
    }

    private void evictCache(Product product) {
        Objects.requireNonNull(cacheManager.getCache("productCache")).evict(product.getSlug());
    }

    public List<MiniProductResponse> getProductNewest(String storeId) {
        var store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        Pageable pageable = PageRequest.of(0, 3);
        return productRepository.findByLastUpdatedAtAndStoreId(store.getId(), pageable).stream()
                .map(product -> MiniProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .slug(product.getSlug())
                        .mainImageUrl(product.getMainImageUrl())
                        .originalPrice(product.getOriginalPrice())
                        .salePrice(product.getSalePrice())
                        .rating(product.getRating())
                        .brandName(product.getBrand().getName())
                        .build())
                .toList();
    }

    public PaginationResponse<ProductBestSellingResponse> getProductBestSelling(
            String page, String size, String limit) {
        int pageNumber;
        int pageSize;
        int productLimit;

        try {
            pageNumber = Integer.parseInt(page);
            pageSize = Integer.parseInt(size);
            productLimit = Integer.parseInt(limit);

            if (pageNumber < 1 || pageSize < 1 || pageNumber > 10 || productLimit < 1 || productLimit > 480) {
                throw new AppException(ErrorCode.PAGE_NOT_FOUND);
            }

            if (pageSize > 48) {
                throw new AppException(ErrorCode.PAGE_SIZE_PRODUCT_TOO_LARGE);
            }

            if (pageSize > productLimit) {
                throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
            }
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.PAGE_NOT_FOUND);
        }

        List<Product> listProductLimit = productRepository.findProductBestSelling(productLimit);

        List<Product> listProductSorted = listProductLimit.stream()
                .sorted(Comparator.comparing(Product::getRating, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Product::getSalePrice, Comparator.naturalOrder())
                        .thenComparing(Product::getCreatedAt, Comparator.naturalOrder()))
                .toList();

        int totalPages = (int) Math.ceil((double) listProductSorted.size() / pageSize);
        if (pageNumber > totalPages) {
            throw new AppException(ErrorCode.PAGE_NOT_FOUND);
        }

        int pageStart = Math.min((pageNumber - 1) * pageSize, listProductSorted.size());
        int pageEnd = Math.min(pageNumber * pageSize, listProductSorted.size());
        List<Product> listProductPaginated = listProductSorted.subList(pageStart, pageEnd);

        List<ProductBestSellingResponse> listProductBestSellingResponse = listProductPaginated.stream()
                .map(product -> {
                    int percentDiscount = (product.getOriginalPrice() != null
                                    && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? product.getOriginalPrice()
                                    .subtract(product.getSalePrice())
                                    .divide(product.getOriginalPrice(), 2, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .intValue()
                            : 0;

                    ProductBestSellingResponse productBestSellingResponse =
                            productMapper.toProductBestSellingResponse(product);
                    productBestSellingResponse.setPercentDiscount(percentDiscount);

                    return productBestSellingResponse;
                })
                .toList();

        return PaginationResponse.<ProductBestSellingResponse>builder()
                .data(listProductBestSellingResponse)
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalElements(listProductSorted.size())
                .hasNext(pageEnd < listProductSorted.size())
                .hasPrevious(pageStart > 0)
                .nextPage(pageEnd < listProductSorted.size() ? pageNumber + 1 : null)
                .previousPage(pageStart > 0 ? pageNumber - 1 : null)
                .build();
    }

    public PaginationResponse<ProductBestInteractionResponse> getProductBestInteraction(
            String page, String size, String limit) {
        int pageNumber;
        int pageSize;
        int productLimit;

        try {
            pageNumber = Integer.parseInt(page);
            pageSize = Integer.parseInt(size);
            productLimit = Integer.parseInt(limit);

            if (pageNumber < 1 || pageSize < 1 || pageNumber > 10 || productLimit < 1 || productLimit > 480) {
                throw new AppException(ErrorCode.PAGE_NOT_FOUND);
            }

            if (pageSize > 48) {
                throw new AppException(ErrorCode.PAGE_SIZE_PRODUCT_TOO_LARGE);
            }

            if (pageSize > productLimit) {
                throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
            }
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.PAGE_NOT_FOUND);
        }

        List<Product> listProductSorted = productRepository.findProductBestInteraction(productLimit);

        int totalPages = (int) Math.ceil((double) listProductSorted.size() / pageSize);
        if (pageNumber > totalPages) {
            throw new AppException(ErrorCode.PAGE_NOT_FOUND);
        }

        int pageStart = Math.min((pageNumber - 1) * pageSize, listProductSorted.size());
        int pageEnd = Math.min(pageNumber * pageSize, listProductSorted.size());
        List<Product> listProductPaginated = listProductSorted.subList(pageStart, pageEnd);

        List<ProductBestInteractionResponse> listProductBestInteractionResponse = listProductPaginated.stream()
                .map(product -> {
                    int percentDiscount = (product.getOriginalPrice() != null
                                    && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0)
                            ? product.getOriginalPrice()
                                    .subtract(product.getSalePrice())
                                    .divide(product.getOriginalPrice(), 2, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .intValue()
                            : 0;

                    ProductBestInteractionResponse productBestInteractionResponse =
                            productMapper.toProductBestInteractionResponse(product);
                    productBestInteractionResponse.setPercentDiscount(percentDiscount);

                    return productBestInteractionResponse;
                })
                .toList();

        return PaginationResponse.<ProductBestInteractionResponse>builder()
                .data(listProductBestInteractionResponse)
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalElements(listProductSorted.size())
                .hasNext(pageEnd < listProductSorted.size())
                .hasPrevious(pageStart > 0)
                .nextPage(pageEnd < listProductSorted.size() ? pageNumber + 1 : null)
                .previousPage(pageStart > 0 ? pageNumber - 1 : null)
                .build();
    }

    @Async
    public void SyncProduct() {
         List<Product> products = productRepository.findAll();
         for (Product product : products) {
             try {
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
                         .sold(product.getSold())
                         .productComponentValues(product.getProductComponentValues().stream()
                                 .map(pc -> new EsProComponentValue(pc.getId(), pc.getValue()))
                                 .toList())
                         .build();
                 productElasticsearchRepository.save(productElasticsearch);
             } catch (Exception e) {
                 log.error("Error syncing product with id {}: {}", product.getId(), e.getMessage());
             }
         }
            log.info("Product sync completed successfully.");
    }
}
