package com.foddy.app.domain.repository

import com.foddy.app.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviewsByTarget(targetId: String): Flow<List<Review>>
    suspend fun addReview(review: Review): Result<Unit>
}
