package com.hkteam.ecommerce_platform.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreDetailResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreResponse;
import com.hkteam.ecommerce_platform.entity.user.Address;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.StoreMapper;
import com.hkteam.ecommerce_platform.repository.AddressRepository;
import com.hkteam.ecommerce_platform.repository.ProductRepository;
import com.hkteam.ecommerce_platform.repository.StoreRepository;
import com.hkteam.ecommerce_platform.util.PageUtils;

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

    @PreAuthorize("hasRole('ADMIN')")
    public PaginationResponse<StoreResponse> getAllStores(
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
        var pageData = storeRepository.searchAllStore(search, search, pageable);

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

        Integer totalFollower = store.getFollowers().size();

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
        response.setTotalFollower(totalFollower);

        return response;
    }
}
