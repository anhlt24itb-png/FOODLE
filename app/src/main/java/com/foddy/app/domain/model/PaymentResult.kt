package com.foddy.app.domain.model

sealed class PaymentResult {
    data class Success(
        val transactionId: String,
        val amount: Long
    ) : PaymentResult()
    
    data class Error(val message: String) : PaymentResult()
}
