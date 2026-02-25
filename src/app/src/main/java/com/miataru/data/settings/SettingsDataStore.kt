package com.miataru.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.miataru.domain.model.MapProvider
import com.miataru.domain.model.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "miataru_settings")

class SettingsDataStore(private val context: Context) {
    val settings: Flow<SettingsState> = context.dataStore.data.map { prefs ->
        val provider = when (prefs[KEY_MAP_PROVIDER]) {
            MapProvider.MAPLIBRE.name -> MapProvider.MAPLIBRE
            else -> MapProvider.GOOGLE
        }
        SettingsState(
            onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false,
            trackAndReportLocation = prefs[KEY_TRACK_AND_REPORT_LOCATION] ?: false,
            enableLocationHistory = prefs[KEY_ENABLE_LOCATION_HISTORY] ?: false,
            allowedDeviceListEnabled = prefs[KEY_ALLOWED_DEVICE_LIST_ENABLED] ?: false,
            serverUrl = prefs[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL,
            ownDeviceId = prefs[KEY_OWN_DEVICE_ID] ?: "",
            ownDeviceKey = prefs[KEY_OWN_DEVICE_KEY] ?: "",
            mapProvider = provider,
        )
    }

    suspend fun updateServerUrl(url: String) {
        context.dataStore.edit { it[KEY_SERVER_URL] = normalizeBaseUrl(url) }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setTrackAndReportLocation(enabled: Boolean) {
        context.dataStore.edit { it[KEY_TRACK_AND_REPORT_LOCATION] = enabled }
    }

    suspend fun setEnableLocationHistory(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ENABLE_LOCATION_HISTORY] = enabled }
    }

    suspend fun setAllowedDeviceListEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ALLOWED_DEVICE_LIST_ENABLED] = enabled }
    }

    suspend fun setOwnDeviceId(deviceId: String) {
        context.dataStore.edit { it[KEY_OWN_DEVICE_ID] = deviceId }
    }

    suspend fun setOwnDeviceKey(deviceKey: String) {
        context.dataStore.edit { it[KEY_OWN_DEVICE_KEY] = deviceKey }
    }

    suspend fun setMapProvider(mapProvider: MapProvider) {
        context.dataStore.edit { it[KEY_MAP_PROVIDER] = mapProvider.name }
    }

    suspend fun ensureOwnDeviceId() {
        context.dataStore.edit { prefs ->
            val existing = prefs[KEY_OWN_DEVICE_ID]
            if (existing.isNullOrBlank()) {
                prefs[KEY_OWN_DEVICE_ID] = UUID.randomUUID().toString()
            }
        }
    }

    companion object {
        const val DEFAULT_SERVER_URL = "https://service.miataru.com"

        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_TRACK_AND_REPORT_LOCATION = booleanPreferencesKey("track_and_report_location")
        private val KEY_ENABLE_LOCATION_HISTORY = booleanPreferencesKey("enable_location_history")
        private val KEY_ALLOWED_DEVICE_LIST_ENABLED = booleanPreferencesKey("allowed_device_list_enabled")
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_OWN_DEVICE_ID = stringPreferencesKey("own_device_id")
        private val KEY_OWN_DEVICE_KEY = stringPreferencesKey("own_device_key")
        private val KEY_MAP_PROVIDER = stringPreferencesKey("map_provider")

        private fun normalizeBaseUrl(input: String): String {
            val trimmed = input.trim().ifBlank { DEFAULT_SERVER_URL }
            return if (trimmed.endsWith('/')) trimmed else "$trimmed/"
        }
    }
}
