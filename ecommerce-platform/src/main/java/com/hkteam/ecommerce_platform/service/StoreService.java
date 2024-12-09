package com.hkteam.ecommerce_platform.service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hkteam.ecommerce_platform.dto.request.StoreRegistrationRequest;
import com.hkteam.ecommerce_platform.dto.request.StoreUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.*;
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
    ProductElasticsearchRepository elasticsearchRepository;

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
        var pageData = storeRepository.searchAllStore(search, search, pageable, tab.equals("blocked") ? true : false);

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

    public StoreInformationResponse getOneStoreById(String id) {
        Store store = storeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        if (store.isBanned()) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }
        String defaultAddressStr = null;
        if (store.getDefaultAddressId() != null) {
            Address defaultAddress = addressRepository
                    .findById(store.getDefaultAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
            List<String> addressParts = new ArrayList<>();
            for (String part : Arrays.asList(
                    defaultAddress.getDetailLocate(),
                    defaultAddress.getDetailAddress(),
                    defaultAddress.getSubDistrict(),
                    defaultAddress.getDistrict(),
                    defaultAddress.getProvince())) {
                if (part != null && !part.isEmpty()) {
                    addressParts.add(part);
                }
            }

            defaultAddressStr = String.join(", ", addressParts);
        }
        Integer totalProduct = productRepository.countByStore(store);

        StoreInformationResponse response = storeMapper.toStoreInformationResponse(store);
        response.setDefaultAddress(defaultAddressStr);
        response.setTotalProduct(totalProduct);

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changeStoreStatus(String storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        store.setBanned(!store.isBanned());
        storeRepository.save(store);
    }

    @PreAuthorize("hasRole('SELLER')")
    public StoreDetailResponse getOneStoreByUserId() {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        Store store = storeRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        String defaultAddressStr = null;
        if (store.getDefaultAddressId() != null) {
            Address defaultAddress = addressRepository
                    .findById(store.getDefaultAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
            List<String> addressParts = new ArrayList<>();
            for (String part : Arrays.asList(
                    defaultAddress.getDetailLocate(),
                    defaultAddress.getDetailAddress(),
                    defaultAddress.getSubDistrict(),
                    defaultAddress.getDistrict(),
                    defaultAddress.getProvince())) {
                if (part != null && !part.isEmpty()) {
                    addressParts.add(part);
                }
            }

            defaultAddressStr = String.join(", ", addressParts);
        }

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

    @Transactional
    @PreAuthorize("hasRole('SELLER')")
    public StoreUpdateResponse updateStore(StoreUpdateRequest request) {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        Store store = storeRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        Address address = addressRepository
                .findById(request.getDefaultAddressId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        Address defaultAddress = addressRepository
                .findByIdAndUserId(address.getId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_BELONG_TO_USER));

        store.setDefaultAddressId(request.getDefaultAddressId());

        storeMapper.updateStore(request, store);

        try {
            store = storeRepository.save(store);
        } catch (DataIntegrityViolationException e) {
            log.info("Error {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        String defaultAddressStr = null;
        if (store.getDefaultAddressId() != null) {
            List<String> addressParts = new ArrayList<>();
            for (String part : Arrays.asList(
                    defaultAddress.getDetailLocate(),
                    defaultAddress.getDetailAddress(),
                    defaultAddress.getSubDistrict(),
                    defaultAddress.getDistrict(),
                    defaultAddress.getProvince())) {
                if (part != null && !part.isEmpty()) {
                    addressParts.add(part);
                }
            }

            defaultAddressStr = String.join(", ", addressParts);
        }

        Integer totalProduct = productRepository.countByStore(store);

        StoreUpdateResponse response = storeMapper.toStoreUpdateResponse(store);
        response.setDefaultAddress(defaultAddressStr);
        response.setTotalProduct(totalProduct);

        return response;
    }

    @PreAuthorize("hasRole('SELLER')")
    public List<Map<String, Object>> getAllAddressOfStore() {
        var user = authenticatedUserUtil.getAuthenticatedUser();

        Store store = user.getStore();
        if (store == null) {
            throw new AppException(ErrorCode.STORE_NOT_FOUND);
        }

        Set<Address> addresses = user.getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        return addresses.stream()
                .map(address -> {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("defaultAddressId", address.getId());
                    addressMap.put(
                            "defaultAddressStr",
                            Stream.of(
                                            address.getDetailLocate(),
                                            address.getDetailAddress(),
                                            address.getSubDistrict(),
                                            address.getDistrict(),
                                            address.getProvince())
                                    .filter(part -> part != null && !part.isEmpty())
                                    .collect(Collectors.joining(", ")));
                    return addressMap;
                })
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void blockStore(String storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        store.setBanned(true);
        store.getProducts().forEach(product -> product.setBlocked(true));
        var esPro = elasticsearchRepository.findByStoreId(storeId);
        esPro.forEach(product -> {
            product.setBlocked(true);
        });
        elasticsearchRepository.saveAll(esPro);
        storeRepository.save(store);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void unblockStore(String storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
        store.setBanned(false);
        store.getProducts().forEach(product -> product.setBlocked(false));
        var esPro = elasticsearchRepository.findByStoreId(storeId);
        esPro.forEach(product -> {
            product.setBlocked(false);
        });
        elasticsearchRepository.saveAll(esPro);
        storeRepository.save(store);
    }
}
