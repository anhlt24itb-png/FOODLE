package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.Address
import com.foddy.app.domain.repository.AddressRepository
import com.foddy.app.presentation.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repository: AddressRepository
) : ViewModel() {

    private val _addresses = MutableStateFlow<UiState<List<Address>>>(UiState.Idle)
    val addresses = _addresses.asStateFlow()

    fun loadAddresses(userId: String) {
        viewModelScope.launch {
            _addresses.value = UiState.Loading
            repository.getAddresses(userId)
                .catch { _addresses.value = UiState.Error(it.message ?: "Error") }
                .collect { _addresses.value = UiState.Success(it) }
        }
    }

    fun addAddress(address: Address) = viewModelScope.launch { repository.addAddress(address) }
    fun updateAddress(address: Address) = viewModelScope.launch { repository.updateAddress(address) }
    fun deleteAddress(id: String) = viewModelScope.launch { repository.deleteAddress(id) }
    fun setDefault(userId: String, id: String) = viewModelScope.launch { repository.setDefaultAddress(userId, id) }
}
