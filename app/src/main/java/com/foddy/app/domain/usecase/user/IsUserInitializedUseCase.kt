package com.foddy.app.domain.usecase.user

import com.foddy.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsUserInitializedUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isInitialized()
    }
}
