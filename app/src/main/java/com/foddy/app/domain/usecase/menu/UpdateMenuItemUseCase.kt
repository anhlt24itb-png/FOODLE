package com.foddy.app.domain.usecase.menu

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.repository.MenuRepository
import javax.inject.Inject

class UpdateMenuItemUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(item: FoodItem) {
        repository.updateMenuItem(item)
    }
}
