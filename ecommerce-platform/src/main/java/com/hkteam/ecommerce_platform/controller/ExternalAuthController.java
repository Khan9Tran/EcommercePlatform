package com.hkteam.ecommerce_platform.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hkteam.ecommerce_platform.dto.request.GoogleTokenRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.GoogleLoginResponse;
import com.hkteam.ecommerce_platform.dto.response.GoogleRegisterResponse;
import com.hkteam.ecommerce_platform.service.ExternalAuthService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/external-auths")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExternalAuthController {

    ExternalAuthService externalAuthService;

    @Operation(summary = "Google register", description = "Api google register account")
    @PostMapping("/google/register")
    ApiResponse<GoogleRegisterResponse> registerWithGoogle(@RequestBody GoogleTokenRequest request) {
        return ApiResponse.<GoogleRegisterResponse>builder()
                .result(externalAuthService.registerWithGoogle(request))
                .build();
    }

    @Operation(summary = "Google register", description = "Api google register account")
    @PostMapping("/google/log-in")
    ApiResponse<GoogleLoginResponse> loginWithGoogle(@RequestBody GoogleTokenRequest request) {
        return ApiResponse.<GoogleLoginResponse>builder()
                .result(externalAuthService.loginWithGoogle(request))
                .build();
    }
}
