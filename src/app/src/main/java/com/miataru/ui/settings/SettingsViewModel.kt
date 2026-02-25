package com.miataru.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.domain.model.MapProvider
import com.miataru.domain.model.SettingsState
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.service.DeviceKeyService
import com.miataru.domain.service.TrackingCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val trackingCoordinator: TrackingCoordinator,
    private val deviceKeyService: DeviceKeyService,
) : ViewModel() {

    val settings: StateFlow<SettingsState> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState(),
        )

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun setTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            trackingCoordinator.setTrackingEnabled(enabled)
        }
    }

    fun setEnableLocationHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEnableLocationHistory(enabled)
        }
    }

    fun setAllowedDeviceList(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowedDeviceListEnabled(enabled)
        }
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setServerUrl(url)
        }
    }

    fun setMapProvider(mapProvider: MapProvider) {
        viewModelScope.launch {
            settingsRepository.setMapProvider(mapProvider)
        }
    }

    fun updateOwnDeviceKey(newKey: String) {
        viewModelScope.launch {
            val result = deviceKeyService.updateOwnDeviceKey(newKey)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to update device key"
            }
        }
    }

    fun rerunOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(false)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
