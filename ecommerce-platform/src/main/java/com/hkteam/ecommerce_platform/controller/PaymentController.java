package com.hkteam.ecommerce_platform.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.hkteam.ecommerce_platform.dto.response.ApiResponse;
import com.hkteam.ecommerce_platform.dto.response.IpnResponse;
import com.hkteam.ecommerce_platform.dto.response.PaymentDetailResponse;
import com.hkteam.ecommerce_platform.service.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Payment Controller")
public class PaymentController {
    PaymentService paymentService;

    // bo
    @GetMapping("/vn-pay-callback")
    public ApiResponse<Void> payCallbackHandler(@RequestParam("vnp_ResponseCode") String status) {
        paymentService.callBack(status);
        return ApiResponse.<Void>builder()
                .message("Payment callback successfully")
                .build();
    }

    @GetMapping("/vnpay_ipn")
    IpnResponse processIpn(@RequestParam Map<String, String> params) {
        log.info("[VNPay Ipn] Params: {}", params);
        return null;
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentDetailResponse> getPayment(@PathVariable String id) {
        return ApiResponse.<PaymentDetailResponse>builder()
                .result(paymentService.getPayment(id))
                .build();
    }
}
