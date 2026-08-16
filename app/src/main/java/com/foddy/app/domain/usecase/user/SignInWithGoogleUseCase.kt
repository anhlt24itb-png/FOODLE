package com.foddy.app.domain.usecase.user

import com.foddy.app.domain.model.User
import com.foddy.app.domain.repository.UserRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> {
        return repository.signInWithGoogle(idToken)
    }
}
