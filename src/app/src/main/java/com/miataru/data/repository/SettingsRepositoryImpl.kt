package com.miataru.data.repository

import com.miataru.data.settings.SettingsDataStore
import com.miataru.domain.model.MapProvider
import com.miataru.domain.model.SettingsState
import com.miataru.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {
    override val settings: Flow<SettingsState> = settingsDataStore.settings

    override suspend fun currentSettings(): SettingsState = settings.first()

    override suspend fun ensureOwnDeviceId() {
        settingsDataStore.ensureOwnDeviceId()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsDataStore.setOnboardingCompleted(completed)
    }

    override suspend fun setTrackAndReportLocation(enabled: Boolean) {
        settingsDataStore.setTrackAndReportLocation(enabled)
    }

    override suspend fun setEnableLocationHistory(enabled: Boolean) {
        settingsDataStore.setEnableLocationHistory(enabled)
    }

    override suspend fun setAllowedDeviceListEnabled(enabled: Boolean) {
        settingsDataStore.setAllowedDeviceListEnabled(enabled)
    }

    override suspend fun setServerUrl(url: String) {
        settingsDataStore.updateServerUrl(url)
    }

    override suspend fun setOwnDeviceId(deviceId: String) {
        settingsDataStore.setOwnDeviceId(deviceId)
    }

    override suspend fun setOwnDeviceKey(deviceKey: String) {
        settingsDataStore.setOwnDeviceKey(deviceKey)
    }

    override suspend fun setMapProvider(mapProvider: MapProvider) {
        settingsDataStore.setMapProvider(mapProvider)
    }
}
