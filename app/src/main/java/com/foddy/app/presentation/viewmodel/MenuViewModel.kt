package com.foddy.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Category
import com.foddy.app.domain.usecase.menu.AddMenuItemUseCase
import com.foddy.app.domain.usecase.menu.GetMenuItemsUseCase
import com.foddy.app.domain.usecase.menu.RemoveMenuItemUseCase
import com.foddy.app.domain.usecase.menu.UpdateMenuItemUseCase
import com.foddy.app.domain.usecase.menu.UploadFoodImageUseCase
import com.foddy.app.domain.usecase.menu.DeleteFoodImageUseCase
import com.foddy.app.core.Resource
import com.foddy.app.presentation.ui.state.UiState
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val getMenuItemsUseCase: GetMenuItemsUseCase,
    private val addMenuItemUseCase: AddMenuItemUseCase,
    private val updateMenuItemUseCase: UpdateMenuItemUseCase,
    private val removeMenuItemUseCase: RemoveMenuItemUseCase,
    private val uploadFoodImageUseCase: UploadFoodImageUseCase,
    private val deleteFoodImageUseCase: DeleteFoodImageUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<FoodItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<FoodItem>>> = _uiState.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val uploadState: StateFlow<Resource<String>> = _uploadState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    init {
        observeCategories()
    }

    fun resetActionState() {
        _actionState.value = UiState.Idle
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getMenuItemsUseCase.executeCategories()
                .catch { e ->
                    println("Error observing categories: ${e.message}")
                }
                .collect {
                    _categories.value = it
                }
        }
    }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            uploadFoodImageUseCase(uri).collect { resource ->
                _uploadState.value = resource
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = Resource.Success("")
    }

    fun observeMenu(restaurantId: String? = null) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Sử dụng invoke() của GetMenuItemsUseCase để lắng nghe Realtime
            getMenuItemsUseCase(restaurantId)
                .catch { e ->
                    _uiState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { items ->
                    _uiState.value = UiState.Success(items)
                    _foodItems.value = items
                }
        }
    }

    fun addFoodItem(item: FoodItem) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                addMenuItemUseCase(item)
                _actionState.value = UiState.Success("Đã thêm món ăn thành công")
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Lỗi khi thêm món ăn")
            }
        }
    }

    fun updateFoodItem(item: FoodItem, oldImageUrl: String? = null) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                updateMenuItemUseCase(item)
                // If update successful and image changed, delete old image
                if (oldImageUrl != null && oldImageUrl != item.imageUrl && oldImageUrl.isNotEmpty()) {
                    deleteFoodImageUseCase(oldImageUrl)
                }
                _actionState.value = UiState.Success("Đã cập nhật món ăn thành công")
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Lỗi khi cập nhật món ăn")
            }
        }
    }

    fun removeFoodItem(item: FoodItem) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                removeMenuItemUseCase(item)
                _actionState.value = UiState.Success("Đã xóa món ăn thành công")
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Lỗi khi xóa món ăn")
            }
        }
    }
}
