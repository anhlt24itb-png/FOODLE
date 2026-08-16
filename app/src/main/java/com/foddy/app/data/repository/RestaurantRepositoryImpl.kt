package com.foddy.app.data.repository

import com.foddy.app.data.local.RestaurantDao
import com.foddy.app.data.mapper.toDomain
import com.foddy.app.data.mapper.toEntity
import com.foddy.app.data.util.networkBoundResource
import com.foddy.app.domain.model.Restaurant
import com.foddy.app.domain.repository.RestaurantRepository
import com.foddy.app.core.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val restaurantDao: RestaurantDao
) : RestaurantRepository {

    override fun getRestaurants(limit: Long, lastVisible: DocumentSnapshot?): Flow<Pair<List<Restaurant>, DocumentSnapshot?>> = callbackFlow {
        var query = db.collection("restaurants")
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        val listener = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.e(e, "Error listening to restaurants")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Restaurant::class.java)?.copy(id = doc.id)
                }
                val lastDoc = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null
                trySend(list to lastDoc)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getRestaurantById(id: String): Restaurant? {
        return try {
            val doc = db.collection("restaurants").document(id).get().await()
            doc.toObject(Restaurant::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Timber.e(e, "Error getting restaurant by id $id")
            null
        }
    }

    override fun getRestaurantsWithCache(): Flow<Resource<List<Restaurant>>> = networkBoundResource(
        query = {
            restaurantDao.getAllRestaurants().map { entities ->
                entities.map { it.toDomain() }
            }
        },
        fetch = {
            db.collection("restaurants")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()
                .documents.mapNotNull { doc ->
                    doc.toObject(Restaurant::class.java)?.copy(id = doc.id)
                }
        },
        saveFetchResult = { restaurants ->
            restaurantDao.deleteAllRestaurants()
            restaurantDao.insertRestaurants(restaurants.map { it.toEntity() })
        }
    )

    override suspend fun updateRestaurant(restaurant: Restaurant): Result<Unit> {
        return try {
            db.collection("restaurants").document(restaurant.id).set(restaurant).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating restaurant")
            Result.failure(e)
        }
    }
}
