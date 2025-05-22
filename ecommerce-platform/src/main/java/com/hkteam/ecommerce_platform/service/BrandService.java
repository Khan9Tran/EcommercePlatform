package com.hkteam.ecommerce_platform.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.BrandCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.BrandUpdateRequest;
import com.hkteam.ecommerce_platform.dto.request.UpdateBrandEsProductRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
import com.hkteam.ecommerce_platform.entity.product.Brand;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.BrandMapper;
import com.hkteam.ecommerce_platform.rabbitmq.RabbitMQConfig;
import com.hkteam.ecommerce_platform.repository.BrandRepository;
import com.hkteam.ecommerce_platform.util.PageUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BrandService {
    BrandRepository brandRepository;
    BrandMapper brandMapper;
    RabbitTemplate rabbitTemplate;

    private static final String CREATED_AT = "createdAt";
    private static final String NAME = "name";
    private static final String ASC = "asc";
    private static final String DESC = "desc";
    private static final String[] SORT_BY = {CREATED_AT, NAME};
    private static final String[] ORDER_BY = {ASC, DESC};

    @PreAuthorize("hasRole('ADMIN')")
    public BrandCreationResponse createBrand(BrandCreationRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }

        Brand brand = brandMapper.toBrand(request);

        try {
            brandRepository.save(brand);
            return brandMapper.toBrandCreationResponse(brand);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while creating brand: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BrandUpdateResponse updateBrand(Long brandId, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        if (brandRepository.existsByNameIgnoreCase(request.getName())
                && !brand.getName().equalsIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.BRAND_DUPLICATE);
        }

        String oldName = brand.getName();
        brandMapper.updateBrandFromRequest(request, brand);
        try {
            brandRepository.save(brand);

            if (!brand.getName().equals(oldName)) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.BRAND_ES_PRODUCT_QUEUE,
                        UpdateBrandEsProductRequest.builder()
                                .name(brand.getName())
                                .id(brand.getId())
                                .isDeleted(Boolean.FALSE)
                                .build());
            }

            return brandMapper.toBrandUpdateResponse(brand);
        } catch (DataIntegrityViolationException e) {
            log.error("Error while updating brand: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        try {
            brandRepository.delete(brand);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BRAND_ES_PRODUCT_QUEUE,
                    UpdateBrandEsProductRequest.builder()
                            .isDeleted(Boolean.TRUE)
                            .id(brandId)
                            .build());
        } catch (Exception e) {
            log.error("Error while deleting brand: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    public PaginationResponse<BrandGetAllResponse> getAllBrand(
            String page, String size, String sortBy, String orderBy, String search) {

        if (!Arrays.asList(SORT_BY).contains(sortBy)) sortBy = null;
        if (!Arrays.asList(ORDER_BY).contains(orderBy)) orderBy = null;
        Sort sortable = (sortBy == null || orderBy == null)
                ? Sort.unsorted()
                : Sort.by(Sort.Direction.fromString(orderBy), sortBy);

        Pageable pageable = PageUtils.createPageable(page, size, sortable);
        var pageData = brandRepository.findAllBrand(search, pageable);

        int pageInt = Integer.parseInt(page);

        PageUtils.validatePageBounds(pageInt, pageData);

        return PaginationResponse.<BrandGetAllResponse>builder()
                .currentPage(pageInt)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? pageInt + 1 : null)
                .previousPage(pageData.hasPrevious() ? pageInt - 1 : null)
                .data(pageData.getContent().stream()
                        .map(brandMapper::toBrandGetAllResponse)
                        .toList())
                .build();
    }

    public List<BrandResponse> getAllBrands(String search) {
        List<Brand> brands;
        if (search == null || search.isEmpty()) {
            brands = brandRepository.findAll();
        } else {
            brands = brandRepository.findByNameIgnoreCase(search);
        }
        return brands.stream().map(brandMapper::toBrandResponse).toList();
    }
}
