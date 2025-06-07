package com.hkteam.ecommerce_platform.controller;

import java.util.HashMap;
import java.util.Map;

import com.hkteam.ecommerce_platform.configuration.VNPayConfig;
import com.hkteam.ecommerce_platform.util.VNPayUtil;
import org.springframework.http.ResponseEntity;
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
    VNPayConfig vnPayConfig;

    // bo
    @GetMapping("/vn-pay-callback")
    public ApiResponse<Void> payCallbackHandler(@RequestParam("vnp_ResponseCode") String status) {
        paymentService.callBack(status);
        return ApiResponse.<Void>builder()
                .message("Payment callback successfully")
                .build();
    }

    @GetMapping("/IPN")
    public ResponseEntity<IpnResponse> processIpn(@RequestParam Map<String, String> params) {
        log.info("[VNPay Ipn] Params: {}", params);

        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            Map<String, String> fields = new HashMap<>(params);


            fields.remove("vnp_SecureHash");


//            // Tạo lại hash từ các params còn lại
//            String signValue = VNPayUtil.getPaymentURL(fields, false);
//            signValue = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), signValue);
//
//            log.info("signValue: {}", signValue);
//            log.info("vnpSecureHash: {}", vnpSecureHash);
//
//            if (!signValue.equals(vnpSecureHash)) {
//                return ResponseEntity.ok(new IpnResponse("97", "Invalid Checksum"));
//            }

            String paymentId = params.get("vnp_TxnRef");
            //String amount = params.get("vnp_Amount");

            PaymentDetailResponse paymentDetail;
            // Giả lập kiểm tra trong DB
            try {
                paymentDetail = paymentService.getPayment(paymentId);
            } catch (Exception e) {
                log.error("Payment not found: {}", paymentId, e);
                return ResponseEntity.ok(new IpnResponse("01", "Order not Found"));
            }

//            if (paymentDetail.getAmount().equals(amount) == false) {
//                return ResponseEntity.ok(new IpnResponse("04", "Invalid Amount"));
//            }


            if (paymentDetail.getStatus().equals("SUCCESS")) {
                return ResponseEntity.ok(new IpnResponse("02", "Order already confirmed"));
            }

            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                paymentService.updateStatus(paymentId);
            } else {
                log.warn("Payment failed with response code: {}", responseCode);
                return ResponseEntity.ok(new IpnResponse("99", "Unknow error"));
            }

            return ResponseEntity.ok(new IpnResponse("00", "Confirm Success"));

        } catch (Exception e) {
            log.error("IPN Error: ", e);
            return ResponseEntity.ok(new IpnResponse("99", "Unknow error"));
        }
    }


    @GetMapping("/{id}")
    public ApiResponse<PaymentDetailResponse> getPayment(@PathVariable String id) {
        return ApiResponse.<PaymentDetailResponse>builder()
                .result(paymentService.getPayment(id))
                .build();
    }

}
