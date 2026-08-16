package com.foddy.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.foddy.app.domain.model.BankAccount
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRGenerator {
    
    fun generateQRBitmap(
        content: String,
        size: Int = 512
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    // Tạo QR theo chuẩn VietQR
    fun generateVietQRBitmap(
        bankAccount: BankAccount,
        orderId: String,
        amount: Long,
        size: Int = 512
    ): Bitmap? {
        val content = buildString {
            append("000201")
            append("010211")
            append("22${bankAccount.bankCode}")
            append("000000")
            append(bankAccount.accountNumber)
            append("0208")
            append("QRIBFTTH")
            append("0303")
            append(amount)
            append("0704")
            append("FODDY$orderId")
            append("0802")
            append("VN")
            append("0910")
            append(bankAccount.accountName.replace(" ", ""))
            append("0D03")
            append("BN")
            append("0D04")
            append(bankAccount.branch.takeIf { it.isNotEmpty() } ?: "HOME")
            append("6304")
        }
        
        return generateQRBitmap(content, size)
    }
}
