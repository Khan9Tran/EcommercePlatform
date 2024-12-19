package com.hkteam.ecommerce_platform.service;

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
import com.hkteam.ecommerce_platform.dto.response.BrandResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
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

    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse createBrand(BrandCreationRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }

        Brand brand = brandMapper.toBrand(request);

        try {
            brandRepository.save(brand);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while creating brand: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return brandMapper.toBrandResponse(brand);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse updateBrand(Long id, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        boolean isDuplicateName = brandRepository.existsByNameIgnoreCase(request.getName())
                && !brand.getName().equalsIgnoreCase(request.getName())
                && !brand.isDeleted();
        if (isDuplicateName) {
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
                                .isDeleted(Boolean.FALSE));
            }

            return brandMapper.toBrandResponse(brand);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while updating brand {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        try {
            brandRepository.delete(brand);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.BRAND_ES_PRODUCT_QUEUE,
                    UpdateBrandEsProductRequest.builder()
                            .isDeleted(Boolean.TRUE)
                            .id(id)
                            .build());
        } catch (Exception e) {
            log.error("Error when delete brand: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<BrandResponse> getAllBrands(
            String pageStr, String sizeStr, String tab, String sort, String search) {
        Sort sortable =
                switch (sort) {
                    case "newest" -> Sort.by("createdAt").descending();
                    case "oldest" -> Sort.by("createdAt").ascending();
                    case "az" -> Sort.by("name").ascending();
                    case "za" -> Sort.by("name").descending();
                    default -> Sort.unsorted();
                };

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sortable);
        var pageData = brandRepository.findByNameContainingIgnoreCase(search, pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);

        return PaginationResponse.<BrandResponse>builder()
                .currentPage(Integer.parseInt(pageStr))
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(pageData.getContent().stream()
                        .map(brandMapper::toBrandResponse)
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

    public BrandResponse getOneBrandById(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toBrandResponse(brand);
    }
}
