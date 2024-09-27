package com.hkteam.ecommerce_platform.service;

import com.hkteam.ecommerce_platform.dto.request.EmailRequest;
import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.EmailResponse;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    UserRepository userRepository;

    public ApiResponse<EmailResponse> updateEmail(EmailRequest request){
        var user = userRepository.findById(request.getUserId()).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        return null;

    }
}
