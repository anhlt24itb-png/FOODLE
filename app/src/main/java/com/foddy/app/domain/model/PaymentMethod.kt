package com.foddy.app.domain.model

enum class PaymentMethod {
    CASH_ON_DELIVERY,     // Tiền mặt khi nhận hàng
    BANK_TRANSFER       // Chuyển khoản qua QR
}

enum class PaymentStatus {
    PENDING,       // Chờ thanh toán
    PAID,          // Đã thanh toán
    CONFIRMED,     // Chủ quán xác nhận
    FAILED,        // Thất bại
    REFUNDED       // Hoàn tiền
}
