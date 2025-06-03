package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import com.hkteam.ecommerce_platform.entity.payment.TransactionStatusHistory;
import com.hkteam.ecommerce_platform.entity.status.TransactionStatus;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.enums.TransactionStatusName;
import com.hkteam.ecommerce_platform.repository.OrderStatusRepository;
import com.hkteam.ecommerce_platform.repository.TransactionRepository;
import com.hkteam.ecommerce_platform.repository.TransactionStatusRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.configuration.VNPayConfig;
import com.hkteam.ecommerce_platform.dto.response.PaymentDetailResponse;
import com.hkteam.ecommerce_platform.entity.payment.Transaction;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.PaymentRepository;
import com.hkteam.ecommerce_platform.util.VNPayUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentService {
    VNPayConfig vnPayConfig;
    PaymentRepository paymentRepository;
    TransactionStatusRepository transactionStatusRepository;
    OrderStatusRepository orderStatusRepository;

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
        log.info("VNPay payment URL: {}", paymentUrl);
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
            if (Boolean.FALSE.equals(transaction
                    .getTransactionStatusHistories()
                    .getLast()
                    .getTransactionStatus()
                    .getName()
                    .equals("SUCCESS"))) {
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

    @Transactional
    public void updateStatus(String paymentId) {
        var payment = paymentRepository.findById(paymentId).orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.getTransactions().forEach(
                transaction -> {
                    TransactionStatus success = transactionStatusRepository
                            .findById(TransactionStatusName.SUCCESS.name())
                            .orElseThrow(() -> new AppException(ErrorCode.UNKNOWN_ERROR));

                    if (transaction.getOrder() == null) {
                        throw new AppException(ErrorCode.ORDER_NOT_FOUND);
                    }

                    transaction.getOrder().getOrderStatusHistories().forEach(
                            orderStatusHistory -> {
                                if (orderStatusHistory.getOrderStatus().getName().equals(OrderStatusName.ON_HOLD.name()) == false) {
                                    throw new AppException(ErrorCode.ORDER_NOT_FOUND);
                                }
                            }
                    );

                    TransactionStatusHistory transactionStatusHistory = TransactionStatusHistory.builder()
                            .transactionStatus(success)
                            .transaction(transaction)
                            .remarks("Update status from VNPay")
                            .build();

                    transaction.getTransactionStatusHistories().add(transactionStatusHistory);

                    var orderStatus = orderStatusRepository.findByName(OrderStatusName.PENDING.name())
                            .orElseThrow(() -> new AppException(ErrorCode.UNKNOWN_ERROR));


                    OrderStatusHistory orderStatusHistory = OrderStatusHistory.builder()
                            .orderStatus(orderStatus)
                            .order(transaction.getOrder())
                            .remarks("Update status from VNPay")
                            .build();

                    transaction.getOrder().getOrderStatusHistories().add(orderStatusHistory);
                }
        );

        paymentRepository.save(payment);
    }
}
