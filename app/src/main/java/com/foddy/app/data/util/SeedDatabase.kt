package com.foddy.app.data.util

import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Restaurant
import com.foddy.app.domain.model.User
import com.foddy.app.domain.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDatabase @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun seedIfNeeded() {
        try {
            val restaurantsCount = try {
                firestore.collection("restaurants").get().await().size()
            } catch (e: Exception) {
                Timber.e(e, "Error checking restaurants count")
                0
            }
            if (restaurantsCount > 0) return

            Timber.d("Seeding database...")

            val restaurants = listOf(
                Restaurant(
                    id = "res_1",
                    name = "The Burger Joint",
                    address = "123 Burger St, Food City",
                    phone = "0123456789",
                    image = "https://images.unsplash.com/photo-1571091718767-18b5b1457add",
                    rating = 4.5,
                    reviewCount = 120,
                    ownerId = "owner_1",
                    category = "Burgers",
                    lat = 10.762622,
                    lng = 106.660172,
                    deliveryTime = "15-25 min",
                    promoTags = listOf("Freeship", "Giảm 20k")
                ),
                Restaurant(
                    id = "res_2",
                    name = "Sushi Master",
                    address = "456 Sushi Ave, Food City",
                    phone = "0987654321",
                    image = "https://images.unsplash.com/photo-1579871494447-9811cf80d66c",
                    rating = 4.8,
                    reviewCount = 500,
                    ownerId = "owner_2",
                    category = "Japanese",
                    lat = 10.772622,
                    lng = 106.670172,
                    deliveryTime = "20-30 min",
                    promoTags = listOf("Bán chạy")
                ),
                Restaurant(
                    id = "res_3",
                    name = "Pizza Heaven",
                    address = "789 Pizza Blvd, Food City",
                    phone = "0112233445",
                    image = "https://images.unsplash.com/photo-1513104890138-7c749659a591",
                    rating = 4.2,
                    reviewCount = 85,
                    ownerId = "owner_3",
                    category = "Italian",
                    lat = 10.752622,
                    lng = 106.650172,
                    deliveryTime = "30-40 min"
                )
            )

            for (res in restaurants) {
                firestore.collection("restaurants").document(res.id).set(res).await()
            }

            val foods = mutableListOf<FoodItem>()
            val categories = listOf("Burgers", "Japanese", "Italian", "Pizza", "Drinks")
            
            for (i in 1..20) {
                val resId = "res_${(i % 3) + 1}"
                val category = when (resId) {
                    "res_1" -> "Burgers"
                    "res_2" -> "Japanese"
                    else -> if (i % 2 == 0) "Pizza" else "Italian"
                }
                
                foods.add(
                    FoodItem(
                        id = "food_$i",
                        name = if (category == "Burgers") "Burger Special $i" else "$category Dish $i",
                        description = "Món ăn thơm ngon, bổ dưỡng với nguyên liệu tươi sạch mỗi ngày. Thưởng thức ngay hương vị đặc biệt từ $category.",
                        price = (20000..150000).random().toDouble(),
                        imageUrl = "https://picsum.photos/seed/${i + 10}/400/300",
                        restaurantId = resId,
                        category = category,
                        isFlashSale = i % 5 == 0,
                        discountPrice = if (i % 5 == 0) 15000.0 else null,
                        soldCount = (10..500).random(),
                        calories = (200..800).random()
                    )
                )
            }

            for (food in foods) {
                firestore.collection("foods").document(food.id).set(food).await()
            }
            
            // Create sample drivers
            val drivers = listOf(
                User(id = "driver_1", name = "Nguyễn Văn Tài", email = "driver1@test.com", role = UserRole.DRIVER.name, phone = "0912345678"),
                User(id = "driver_2", name = "Trần Thị Xế", email = "driver2@test.com", role = UserRole.DRIVER.name, phone = "0987654321")
            )
            for (driver in drivers) {
                firestore.collection("users").document(driver.id).set(driver).await()
            }

            Timber.d("Database seeded successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error seeding database")
        }
    }
}
