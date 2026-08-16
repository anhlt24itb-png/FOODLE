package com.foddy.app.domain.usecase.menu

import com.foddy.app.core.Resource
import com.foddy.app.domain.repository.StorageRepository
import javax.inject.Inject

class DeleteFoodImageUseCase @Inject constructor(
    private val repository: StorageRepository
) {
    suspend operator fun invoke(imageUrl: String): Resource<Unit> {
        return repository.deleteImage(imageUrl)
    }
}
