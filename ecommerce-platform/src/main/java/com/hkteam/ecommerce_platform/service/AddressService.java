package com.hkteam.ecommerce_platform.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.AddressCreationRequest;
import com.hkteam.ecommerce_platform.dto.request.AddressUpdateRequest;
import com.hkteam.ecommerce_platform.dto.response.AddressResponse;
import com.hkteam.ecommerce_platform.entity.user.Address;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.AddressMapper;
import com.hkteam.ecommerce_platform.repository.AddressRepository;
import com.hkteam.ecommerce_platform.repository.UserRepository;
import com.hkteam.ecommerce_platform.util.StringUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AddressService {
    AddressRepository addressRepository;
    UserRepository userRepository;
    AddressMapper addressMapper;

    public AddressResponse createAddress(AddressCreationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        var user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String detailLocate = request.getDetailLocate().trim();
        detailLocate = StringUtils.convertEmptyToNull(detailLocate);

        Address address = addressMapper.toAddress(request);
        address.setRecipientName(request.getRecipientName().trim());
        address.setPhone(request.getPhone().trim());
        address.setProvince(request.getProvince().trim());
        address.setDistrict(request.getDistrict().trim());
        address.setDetailAddress(request.getDetailAddress().trim());
        address.setDetailLocate(detailLocate);
        address.setUser(user);

        try {
            address = addressRepository.save(address);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.ADDRESS_EXISTED);
        }

        return addressMapper.toAddressResponse(address);
    }

    public AddressResponse updateAddress(Long id, AddressUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        var user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Address address =
                addressRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        address.setRecipientName(request.getRecipientName().trim());
        address.setPhone(request.getPhone().trim());
        address.setProvince(request.getProvince().trim());
        address.setDistrict(request.getDistrict().trim());
        address.setDetailAddress(request.getDetailAddress().trim());
        address.setDetailLocate(request.getDetailLocate().trim());
        address.setUser(user);

        return addressMapper.toAddressResponse(addressRepository.save(address));
    }

    public void deleteAddress(Long id) {
        Address address =
                addressRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        addressRepository.delete(address);
    }

    public AddressResponse getOneAddressById(Long id) {
        Address address =
                addressRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        return addressMapper.toAddressResponse(address);
    }
}
