package com.foddy.app.domain.repository

import android.net.Uri
import com.foddy.app.core.Resource
import kotlinx.coroutines.flow.Flow

interface StorageRepository {
    fun uploadImage(uri: Uri, path: String): Flow<Resource<String>>
    suspend fun deleteImage(imageUrl: String): Resource<Unit>
}
