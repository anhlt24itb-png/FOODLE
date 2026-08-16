package com.foddy.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.foddy.app.core.Resource
import com.foddy.app.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : StorageRepository {

    override fun uploadImage(uri: Uri, path: String): Flow<Resource<String>> = callbackFlow {
        try {
            trySend(Resource.Loading())

            // 1. Compression
            val compressedImage = compressImage(uri)
            if (compressedImage == null) {
                trySend(Resource.Error("Failed to compress image"))
                close()
                return@callbackFlow
            }

            val fileName = UUID.randomUUID().toString() + ".webp"
            val storageRef = storage.reference.child("$path/$fileName")

            val uploadTask = storageRef.putBytes(compressedImage)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                // Optionally handle progress
            }.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    trySend(Resource.Success(downloadUri.toString()))
                    close()
                }.addOnFailureListener {
                    trySend(Resource.Error(it.message ?: "Failed to get download URL"))
                    close()
                }
            }.addOnFailureListener {
                trySend(Resource.Error(it.message ?: "Upload failed"))
                close()
            }

        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "An unknown error occurred"))
            close()
        }
        awaitClose()
    }

    override suspend fun deleteImage(imageUrl: String): Resource<Unit> {
        return try {
            storage.getReferenceFromUrl(imageUrl).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete image")
        }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            // Compress to WebP for production efficiency
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 80, outputStream)
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
            }
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
