package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.hkteam.ecommerce_platform.dto.response.PaymentDetailResponse;
import com.hkteam.ecommerce_platform.entity.payment.Transaction;
import com.hkteam.ecommerce_platform.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.configuration.VNPayConfig;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.util.VNPayUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentService {
    VNPayConfig vnPayConfig;
    PaymentRepository paymentRepository;

    public String createVnPayPayment(BigDecimal totalPrice, HttpServletRequest request, String code) {
        BigDecimal amount = totalPrice.multiply(new BigDecimal("100")).setScale(0, RoundingMode.DOWN);
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig(code);
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return paymentUrl;
    }

    public void callBack(String status) {
        log.info("Payment callback status: {}", status);
        if (!status.equals("00")) {
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        }
    }

    public PaymentDetailResponse getPayment(String id) {
        var payment = paymentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        boolean hasPay = true;
        for (Transaction transaction : payment.getTransactions()) {
            if (Boolean.FALSE.equals(transaction.getTransactionStatusHistories().getLast().getTransactionStatus().getName().equals("SUCCESS"))) {
                hasPay = false;
                break;
            }
        }
        return PaymentDetailResponse.builder()
                .id(payment.getId())
                .paymentMethod(payment.getPaymentMethod().name())
                .amount(payment.getAmount().toString())
                .status(hasPay ? "SUCCESS" : "WAITING")
                .build();
    }
}
