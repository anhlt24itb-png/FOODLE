package com.foddy.app.domain.usecase.menu

import android.net.Uri
import com.foddy.app.core.Resource
import com.foddy.app.domain.repository.StorageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadFoodImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    operator fun invoke(uri: Uri): Flow<Resource<String>> {
        return storageRepository.uploadImage(uri, "foods")
    }
}
