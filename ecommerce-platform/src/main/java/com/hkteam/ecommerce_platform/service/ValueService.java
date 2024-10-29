package com.hkteam.ecommerce_platform.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.UpdateValueRequest;
import com.hkteam.ecommerce_platform.dto.response.ValueDetailResponse;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.ValueRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ValueService {
    ValueRepository valueRepository;

    @PreAuthorize("hasRole('SELLER')")
    public ValueDetailResponse updateValue(Long id, UpdateValueRequest request) {

        var value = valueRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.VALUE_NOT_FOUND));
        return ValueDetailResponse.builder()
                .id(request.getId())
                .value(request.getValue())
                .build();
    }
}
