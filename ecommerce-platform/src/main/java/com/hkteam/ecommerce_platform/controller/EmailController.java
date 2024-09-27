package com.hkteam.ecommerce_platform.controller;

import com.hkteam.ecommerce_platform.dto.request.EmailRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Component Controller")
public class EmailController {
    @PostMapping("/user/")
    ApiResponse<Void> updateEmail(@RequestBody EmailRequest request)
    {

    }
}
