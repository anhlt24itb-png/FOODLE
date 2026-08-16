package com.foddy.app.domain.usecase.user

import com.foddy.app.domain.repository.UserRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
