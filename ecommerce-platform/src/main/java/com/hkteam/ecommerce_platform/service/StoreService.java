package com.hkteam.ecommerce_platform.service;

import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.StoreRegistrationRequest;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreRegistrationResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreResponse;
import com.hkteam.ecommerce_platform.entity.user.Address;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.enums.TypeSlug;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.StoreMapper;
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
public class StoreService {
    StoreRepository storeRepository;
    StoreMapper storeMapper;
    AddressRepository addressRepository;
    ProductRepository productRepository;
    AuthenticatedUserUtil authenticatedUserUtil;
    RoleRepository roleRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<StoreResponse> getAllStores(
            String pageStr, String sizeStr, String tab, String sortDate, String sortName) {
        Sort sort = Sort.unsorted();
        if (sortDate.equals("newest")) sort = Sort.by("createdAt").descending();
        else if (sortDate.equals("oldest")) sort = Sort.by("createdAt").ascending();
        else if (!sortDate.isEmpty()) throw new AppException(ErrorCode.INVALID_REQUEST);

        if (sortName.equals("za")) sort = sort.and(Sort.by("name").descending());
        else if (sortName.equals("az")) sort = sort.and(Sort.by("name").ascending());
        else if (!sortName.isEmpty()) throw new AppException(ErrorCode.INVALID_REQUEST);

        log.info(sort.toString());

        Pageable pageable = PageUtils.createPageable(pageStr, sizeStr, sort);
        var pageData = storeRepository.findAll(pageable);

        int page = Integer.parseInt(pageStr);

        PageUtils.validatePageBounds(page, pageData);

        return PaginationResponse.<StoreResponse>builder()
                .currentPage(Integer.parseInt(pageStr))
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .hasNext(pageData.hasNext())
                .hasPrevious(pageData.hasPrevious())
                .nextPage(pageData.hasNext() ? page + 1 : null)
                .previousPage(pageData.hasPrevious() ? page - 1 : null)
                .data(pageData.getContent().stream()
                        .map(storeMapper::toStoreResponse)
                        .toList())
                .build();
    }

    public StoreDetailResponse getOneStoreById(String id) {
        Store store = storeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        Address defaultAddress = addressRepository
                .findById(store.getDefaultAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        String defaultAddressStr = String.join(
                ", ",
                defaultAddress.getDetailLocate(),
                defaultAddress.getDetailAddress(),
                defaultAddress.getDistrict(),
                defaultAddress.getProvince());
        Integer totalProduct = productRepository.countByStore(store);

        StoreDetailResponse response = storeMapper.toStoreDetailResponse(store);
        response.setDefaultAddress(defaultAddressStr);
        response.setTotalProduct(totalProduct);

        return response;
    }

    public StoreRegistrationResponse registerStore(StoreRegistrationRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        if (!Objects.isNull(user.getStore())) {
            throw new AppException(ErrorCode.SELLER_ALREADY_REGISTER);
        }

        var sellerRole = roleRepository
                .findByName(RoleName.SELLER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.getRoles().add(sellerRole);

        Store store = storeMapper.toStore(request);
        store.setSlug(SlugUtils.getSlug(request.getName(), TypeSlug.STORE));
        store.setUser(user);

        try {
            storeRepository.save(store);
        } catch (DataIntegrityViolationException e) {
            log.info("Error while creating category: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return storeMapper.toStoreRegistrationResponse(store);
    }
}
