package com.foddy.app.domain.usecase.user

import com.foddy.app.domain.repository.UserRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun updateName(name: String) = repository.updateName(name)
    suspend fun updateAvatar(imageUrl: String) = repository.updateAvatar(imageUrl)
    suspend fun updatePhone(phone: String) = repository.updatePhone(phone)
}
