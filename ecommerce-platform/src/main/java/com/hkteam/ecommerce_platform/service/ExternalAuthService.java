package com.hkteam.ecommerce_platform.service;

import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.request.GoogleTokenRequest;
import com.hkteam.ecommerce_platform.dto.response.GoogleLoginResponse;
import com.hkteam.ecommerce_platform.dto.response.GoogleRegisterResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExternalAuthService {
    public GoogleRegisterResponse registerWithGoogle(GoogleTokenRequest request) {

        return null;
    }

    public GoogleLoginResponse loginWithGoogle(GoogleTokenRequest request) {

        return null;
    }
}
