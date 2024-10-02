package com.hkteam.ecommerce_platform.controller;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.AuthenticationResponse;
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
    @PostMapping("/authentication/google")
    ApiResponse<AuthenticationResponse> googleAuthenticate(@RequestParam("code") String code) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(externalAuthService.googleAuthenticate(code))
                .build();
    }
}
