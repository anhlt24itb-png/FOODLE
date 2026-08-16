package com.foddy.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    /**
     * Nén ảnh từ Uri và chuyển sang chuỗi Base64 Data URL.
     * @param context Context để truy cập contentResolver
     * @param uri Uri của ảnh
     * @param maxWidth Chiều rộng tối đa mong muốn
     * @param maxHeight Chiều cao tối đa mong muốn
     * @param quality Chất lượng nén (0-100)
     * @return Chuỗi Base64 Data URL (data:image/jpeg;base64,...) hoặc null nếu lỗi
     */
    fun compressImageToBase64(
        context: Context,
        uri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 70
    ): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            // Tính toán tỷ lệ nén để giữ aspect ratio
            val ratio = Math.min(
                maxWidth.toFloat() / originalBitmap.width,
                maxHeight.toFloat() / originalBitmap.height
            )
            
            val finalBitmap = if (ratio < 1f) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (originalBitmap.width * ratio).toInt(),
                    (originalBitmap.height * ratio).toInt(),
                    true
                )
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
            
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
