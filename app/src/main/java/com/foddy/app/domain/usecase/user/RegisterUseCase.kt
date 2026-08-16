package com.foddy.app.domain.usecase.user

import com.foddy.app.domain.model.User
import com.foddy.app.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String, role: String): Result<User> {
        return repository.register(name, email, password, role)
    }
}
