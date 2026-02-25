package com.miataru.domain.repository

import com.miataru.domain.model.VisitorEvent
import kotlinx.coroutines.flow.Flow

interface VisitorRepository {
    val visitors: Flow<List<VisitorEvent>>

    suspend fun refreshVisitorHistory(amount: Int = 50): Result<Unit>
    suspend fun setVisitorIgnored(visitorEventId: Long, ignored: Boolean): Result<Unit>
    suspend fun clearAll(): Result<Unit>
}
