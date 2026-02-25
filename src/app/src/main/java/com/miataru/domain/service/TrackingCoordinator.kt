package com.miataru.domain.service

import kotlinx.coroutines.flow.Flow

interface TrackingCoordinator {
    val trackingEnabled: Flow<Boolean>

    suspend fun setTrackingEnabled(enabled: Boolean)
    suspend fun triggerImmediateUpload()
}
