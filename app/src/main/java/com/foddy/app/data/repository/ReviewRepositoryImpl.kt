package com.foddy.app.data.repository

import com.foddy.app.domain.model.Review
import com.foddy.app.domain.repository.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override fun getReviewsByTarget(targetId: String): Flow<List<Review>> = callbackFlow {
        val listener = firestore.collection("reviews")
            .whereEqualTo("targetId", targetId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.copy(id = doc.id)
                }
                trySend(reviews ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addReview(review: Review): Result<Unit> = try {
        firestore.collection("reviews").add(review).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
