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

            // Xoá 2 trường không dùng khi tạo hash
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");

            // Tạo lại hash từ các params còn lại
            String signValue = VNPayUtil.getPaymentURL(fields, true);

            if (!signValue.equals(vnpSecureHash)) {
                return ResponseEntity.ok(new IpnResponse("97", "Invalid Checksum"));
            }

            String paymentId = params.get("vnp_TxnRef");
            String amount = params.get("vnp_Amount");
            // Giả lập kiểm tra trong DB
            boolean checkOrderId = true; // kiểm tra vnp_TxnRef có trong DB
            boolean checkAmount = true; // kiểm tra số tiền có khớp không
            boolean checkOrderStatus = true; // đơn hàng đang chờ xử lý

            if (!checkOrderId) {
                return ResponseEntity.ok(new IpnResponse("01", "Order not Found"));
            }

            if (!checkAmount) {
                return ResponseEntity.ok(new IpnResponse("04", "Invalid Amount"));
            }

            if (!checkOrderStatus) {
                return ResponseEntity.ok(new IpnResponse("02", "Order already confirmed"));
            }

            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                // Cập nhật trạng thái đơn hàng = 1 (thành công)
                // orderService.updateStatus(orderId, 1);
            } else {
                // Cập nhật trạng thái đơn hàng = 2 (thất bại)
                // orderService.updateStatus(orderId, 2);
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
