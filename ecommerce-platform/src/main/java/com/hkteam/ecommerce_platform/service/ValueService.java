package com.hkteam.ecommerce_platform.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.UpdateValueRequest;
import com.hkteam.ecommerce_platform.dto.response.ValueDetailResponse;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.mapper.ValuesMapper;
import com.hkteam.ecommerce_platform.repository.ValueRepository;
import com.hkteam.ecommerce_platform.util.AuthenticatedUserUtil;

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
    AuthenticatedUserUtil authenticatedUserUtil;
    ValuesMapper valuesMapper;

    @PreAuthorize("hasRole('SELLER')")
    public ValueDetailResponse updateValue(Long id, UpdateValueRequest request) {

        var value = valueRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.VALUE_NOT_FOUND));

        if (!authenticatedUserUtil.isOwner(value)) throw new AppException(ErrorCode.UNAUTHORIZED);

        valuesMapper.updateValueFromRequest(request, value);

        try {
            valueRepository.save(value);
        } catch (Exception e) {
            log.error("Error while updating value", e);
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }

        return valuesMapper.toValueDetailResponse(valueRepository.save(value));
    }
}
