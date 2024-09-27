package com.hkteam.ecommerce_platform.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.BrandCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.BrandUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.BrandResponse;
import com.hkteam.ecommerce_platform.entity.product.Brand;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.BrandMapper;
import com.hkteam.ecommerce_platform.repository.BrandRepository;

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

        if (description.isEmpty()) {
            description = null;
        }

        Brand brand = brandMapper.toBrand(request);

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

        if (description.isEmpty()) {
            description = null;
        }

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

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toBrandResponse)
                .toList();
    }

    public BrandResponse getOneBrandById(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toBrandResponse(brand);
    }
}
