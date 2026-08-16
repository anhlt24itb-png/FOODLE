package com.foddy.app.domain.model

data class BankAccount(
    val bankName: String,          // Ví dụ: "Vietcombank"
    val bankCode: String,       // VCB
    val accountNumber: String,    // Số tài khoản
    val accountName: String,    // Tên chủ tài khoản
    val branch: String = ""    // Chi nhánh
) {
    // Tạo nội dung chuyển khoản
    fun getTransferContent(orderId: String): String = "FODDY$orderId"
    
    // Tạo QR code content (theo chuẩn VietQR)
    fun getQRContent(orderId: String, amount: Long): String {
        val content = getTransferContent(orderId)
        return "|$bankCode|$accountNumber|$accountName|$amount|$content||0"
    }
}

enum class VietQRBanks(val code: String, val kha: String) {
    VCB("VCB", "Vietcombank"),
    ACB("ACB", "Á Châu"),
    BIDV("BIDV", "BIDV"),
    TPB("TPB", "TPBank"),
    MBB("MBB", "MB Bank"),
    VTB("VTB", "ViettinBank"),
    OCB("OCB", "OCB"),
    SHB("SHB", "SHB")
}
