package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.User
import com.foddy.app.domain.usecase.user.*
import com.foddy.app.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isUserInitializedUseCase: IsUserInitializedUseCase,
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState(isLoading = true))
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val forgotPasswordState: StateFlow<UiState<Unit>> = _forgotPasswordState.asStateFlow()

    // Compatibility property for existing UI screens
    val user: StateFlow<User> = _uiState
        .map { it.user ?: User(id = "", email = "", name = "") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User(id = "", email = "", name = ""))

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            launch {
                getCurrentUserUseCase().collect { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false
                    )
                }
            }
            
            launch {
                isUserInitializedUseCase().collect { initialized ->
                    _isInitialized.value = initialized
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, role: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            registerUseCase(name, email, password, role)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(user = it, isLoading = false)
                    onComplete(true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message, isLoading = false)
                    onComplete(false)
                }
        }
    }

    fun loginCloud(email: String, password: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            loginUseCase(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(user = it, isLoading = false)
                    onComplete(true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message, isLoading = false)
                    onComplete(false)
                }
        }
    }

    fun signInWithGoogle(idToken: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            signInWithGoogleUseCase(idToken)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(user = it, isLoading = false)
                    onComplete(true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message, isLoading = false)
                    onComplete(false)
                }
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            updateProfileUseCase.updateName(newName)
        }
    }

    fun updateAvatar(imageUrl: String) {
        viewModelScope.launch {
            updateProfileUseCase.updateAvatar(imageUrl)
        }
    }

    fun updatePhone(phone: String) {
        viewModelScope.launch {
            updateProfileUseCase.updatePhone(phone)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = UiState.Loading
            forgotPasswordUseCase(email)
                .onSuccess {
                    _forgotPasswordState.value = UiState.Success(Unit)
                }
                .onFailure {
                    _forgotPasswordState.value = UiState.Error(it.message ?: "Unknown error")
                }
        }
    }
}
