package com.miataru.domain.repository

import com.miataru.domain.model.MapProvider
import com.miataru.domain.model.SettingsState
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<SettingsState>

    suspend fun currentSettings(): SettingsState
    suspend fun ensureOwnDeviceId()
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setTrackAndReportLocation(enabled: Boolean)
    suspend fun setEnableLocationHistory(enabled: Boolean)
    suspend fun setAllowedDeviceListEnabled(enabled: Boolean)
    suspend fun setServerUrl(url: String)
    suspend fun setOwnDeviceId(deviceId: String)
    suspend fun setOwnDeviceKey(deviceKey: String)
    suspend fun setMapProvider(mapProvider: MapProvider)
}
