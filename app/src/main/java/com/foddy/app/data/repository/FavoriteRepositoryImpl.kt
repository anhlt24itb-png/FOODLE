package com.foddy.app.data.repository

import com.foddy.app.domain.model.Favorite
import com.foddy.app.domain.repository.FavoriteRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FavoriteRepository {

    override fun getFavorites(userId: String): Flow<List<Favorite>> = callbackFlow {
        val listener = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val favorites = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Favorite::class.java)?.copy(id = doc.id)
                }
                trySend(favorites ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun toggleFavorite(userId: String, targetId: String, type: String, name: String, image: String): Result<Unit> = try {
        val query = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .whereEqualTo("targetId", targetId)
            .get().await()

        if (query.isEmpty) {
            val fav = Favorite(
                userId = userId, 
                targetId = targetId, 
                type = type,
                targetName = name,
                targetImage = image
            )
            firestore.collection("favorites").add(fav).await()
        } else {
            query.documents.first().reference.delete().await()
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun isFavorite(userId: String, targetId: String): Boolean = try {
        val query = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .whereEqualTo("targetId", targetId)
            .get().await()
        !query.isEmpty
    } catch (e: Exception) {
        false
    }
}
