package com.hkteam.ecommerce_platform.controller;

import java.text.ParseException;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.ConfirmResetPassordRequest;
import com.hkteam.ecommerce_platform.dto.request.EmailRequest;
import com.hkteam.ecommerce_platform.dto.request.ResetPasswordRequest;
import com.hkteam.ecommerce_platform.dto.request.VerifyEmailRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.EmailResponse;
import com.hkteam.ecommerce_platform.service.EmailService;
import com.nimbusds.jose.JOSEException;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Email Controller")
public class EmailController {
    EmailService emailService;

    @PostMapping("/user/")
    ApiResponse<EmailResponse> updateEmail(@RequestBody @Valid EmailRequest request) {
        return ApiResponse.<EmailResponse>builder()
                .result(emailService.updateEmail(request))
                .build();
    }

    @PostMapping("/send-verification")
    ApiResponse<Void> sendMailValidation() throws JOSEException {
        emailService.sendMailValidation();
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/verify")
    ApiResponse<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequest token) throws ParseException, JOSEException {
        emailService.verifyEmail(token);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/reset-request")
    ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) throws JOSEException {
        emailService.sendPasswordResetEmail(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody @Valid ConfirmResetPassordRequest request)
            throws JOSEException, ParseException {
        emailService.confirmResetPassword(request);
        return ApiResponse.<Void>builder().build();
    }
}
