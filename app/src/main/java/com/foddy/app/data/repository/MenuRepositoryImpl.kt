package com.foddy.app.data.repository

import com.foddy.app.data.local.FoodItemDao
import com.foddy.app.data.mapper.toDomain
import com.foddy.app.data.mapper.toEntity
import com.foddy.app.data.util.networkBoundResource
import com.foddy.app.domain.model.FoodItem
import com.foddy.app.domain.model.Category
import com.foddy.app.domain.repository.MenuRepository
import com.foddy.app.core.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val foodItemDao: FoodItemDao,
    private val db: FirebaseFirestore
) : MenuRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getMenuItems(restaurantId: String?): Flow<List<FoodItem>> = callbackFlow {
        // Trả về dữ liệu từ local trước nếu có
        scope.launch {
            foodItemDao.getAllItems().collect { entities ->
                val localItems = entities.map { it.toDomain() }
                if (localItems.isNotEmpty()) {
                    if (restaurantId == null) {
                        trySend(localItems)
                    } else {
                        trySend(localItems.filter { it.restaurantId == restaurantId })
                    }
                }
            }
        }

        val collection = if (restaurantId != null) {
            db.collection("foods").whereEqualTo("restaurantId", restaurantId)
        } else {
            db.collection("foods")
        }

        val listener = collection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(FoodItem::class.java)
                    // Map "imageUrl" from Firestore if it exists, or use "image" if older data
                    val imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: ""
                    item?.copy(id = doc.id, imageUrl = imageUrl)
                }
                
                // Cập nhật local database (chỉ nếu lấy toàn bộ hoặc có logic sync phù hợp)
                if (restaurantId == null) {
                    scope.launch {
                        foodItemDao.clearAll()
                        foodItemDao.insertAll(items.map { it.toEntity() })
                    }
                }

                trySend(items)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addMenuItem(item: FoodItem) {
        val itemData = item.copy(id = "")
        db.collection("foods").add(itemData).await()
    }

    override suspend fun updateMenuItem(item: FoodItem) {
        db.collection("foods").document(item.id).set(item).await()
    }

    override suspend fun removeMenuItem(item: FoodItem) {
        db.collection("foods").document(item.id).delete().await()
    }

    override fun getMenuItemsWithCache(restaurantId: String?): Flow<Resource<List<FoodItem>>> = networkBoundResource(
        query = {
            if (restaurantId == null) {
                foodItemDao.getAllItems().map { entities -> entities.map { it.toDomain() } }
            } else {
                foodItemDao.getItemsByRestaurant(restaurantId).map { entities -> entities.map { it.toDomain() } }
            }
        },
        fetch = {
            val query = if (restaurantId != null) {
                db.collection("foods").whereEqualTo("restaurantId", restaurantId)
            } else {
                db.collection("foods")
            }
            query.get().await().documents.mapNotNull { doc ->
                val item = doc.toObject(FoodItem::class.java)
                val imageUrl = doc.getString("imageUrl") ?: doc.getString("image") ?: ""
                item?.copy(id = doc.id, imageUrl = imageUrl)
            }
        },
        saveFetchResult = { items ->
            if (restaurantId == null) {
                foodItemDao.clearAll()
            }
            foodItemDao.insertAll(items.map { it.toEntity() })
        }
    )

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val listener = db.collection("categories").addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val categories = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }
                trySend(categories)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addCategory(category: Category) {
        db.collection("categories").add(category).await()
    }

    override suspend fun updateCategory(category: Category) {
        db.collection("categories").document(category.id).set(category).await()
    }

    override suspend fun deleteCategory(categoryId: String) {
        db.collection("categories").document(categoryId).delete().await()
    }
}
