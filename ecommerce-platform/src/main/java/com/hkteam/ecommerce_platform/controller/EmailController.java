package com.hkteam.ecommerce_platform.controller;

import jakarta.mail.MessagingException;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.request.EmailRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.EmailResponse;
import com.hkteam.ecommerce_platform.service.EmailService;
import com.nimbusds.jose.JOSEException;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Email Controller")
public class EmailController {
    EmailService emailService;

    @PostMapping("/user/")
    ApiResponse<EmailResponse> updateEmail(@RequestBody EmailRequest request) {
        return ApiResponse.<EmailResponse>builder()
                .result(emailService.updateEmail(request))
                .build();
    }

    @PostMapping("/send-verification")
    ApiResponse<Void> sendMailValidation() throws MessagingException, JOSEException {
        emailService.sendMailValidation();
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/verify")
    ApiResponse<Void> verifyEmail(@RequestParam("token") String token) throws ParseException, JOSEException {
        emailService.verifyEmail(token);
        return ApiResponse.<Void>builder().build();
    }
}
