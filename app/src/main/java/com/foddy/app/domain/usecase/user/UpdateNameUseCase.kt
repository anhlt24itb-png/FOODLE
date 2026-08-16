package com.foddy.app.domain.usecase.user

import com.foddy.app.domain.repository.UserRepository
import javax.inject.Inject

class UpdateNameUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(name: String): Result<Unit> {
        return repository.updateName(name)
    }
}
