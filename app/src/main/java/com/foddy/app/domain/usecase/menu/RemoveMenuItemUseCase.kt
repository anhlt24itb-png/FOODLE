package com.foddy.app.domain.usecase.menu

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.repository.MenuRepository
import javax.inject.Inject

class RemoveMenuItemUseCase @Inject constructor(
    private val repository: MenuRepository
) {
    suspend operator fun invoke(item: FoodItem) = repository.removeMenuItem(item)
}
