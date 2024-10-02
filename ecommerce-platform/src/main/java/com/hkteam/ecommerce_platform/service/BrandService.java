package com.hkteam.ecommerce_platform.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.BrandCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.BrandUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.BrandResponse;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.entity.product.Brand;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.BrandMapper;
import com.hkteam.ecommerce_platform.repository.BrandRepository;
import com.hkteam.ecommerce_platform.util.PageUtils;
import com.hkteam.ecommerce_platform.util.StringUtils;

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

    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse createBrand(BrandCreationRequest request) {
        String name = request.getName().trim().toLowerCase();
        String description = request.getDescription().trim();

        if (brandRepository.existsByNameIgnoreCase(name)) {
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }

        description = StringUtils.convertEmptyToNull(description);

        Brand brand = brandMapper.toBrand(request);

        brand.setName(request.getName().trim());
        brand.setDescription(description);

        try {
            brand = brandRepository.save(brand);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }

        return brandMapper.toBrandResponse(brand);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public BrandResponse updateBrand(Long id, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        String name = request.getName().trim().toLowerCase();
        String description = request.getDescription().trim();

        boolean isDuplicateName =
                brandRepository.existsByNameIgnoreCase(name) && !brand.getName().equalsIgnoreCase(name);
        if (isDuplicateName) {
            throw new AppException(ErrorCode.BRAND_DUPLICATE);
        }

        description = StringUtils.convertEmptyToNull(description);

        brand.setName(request.getName().trim());
        brand.setDescription(description);

        brand = brandRepository.save(brand);

        return brandMapper.toBrandResponse(brand);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
    }

    public PaginationResponse<BrandResponse> getAllBrands(String pageStr, String sizeStr) {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sort);

        var pageData = brandRepository.findAll(pageable);
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

    public BrandResponse getOneBrandById(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toBrandResponse(brand);
    }
}
