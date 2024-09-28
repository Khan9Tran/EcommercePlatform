package com.hkteam.ecommerce_platform.enums;

public enum EmailValidationStatus {
    UNVERIFIED,
    PENDING, // Chờ xác thực email
    VERIFIED, // Email đã xác thực thành công
    EXPIRED, // Token hết hạn
    FAILED, // Xác thực thất bại
    ALREADY_VERIFIED, // Email đã được xác thực trước đó
    INVALID_TOKEN, // Token không hợp lệ
    RESEND_REQUESTED // Yêu cầu gửi lại email xác thực
}
