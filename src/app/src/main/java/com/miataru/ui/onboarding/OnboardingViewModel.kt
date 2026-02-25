package com.miataru.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.domain.model.MapProvider
import com.miataru.domain.model.SettingsState
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.service.TrackingCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val trackingCoordinator: TrackingCoordinator,
) : ViewModel() {

    val settings: StateFlow<SettingsState> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState(),
        )

    fun setTrackAndReportLocation(enabled: Boolean) {
        viewModelScope.launch {
            trackingCoordinator.setTrackingEnabled(enabled)
        }
    }

    fun retryTrackingActivationIfEnabled() {
        viewModelScope.launch {
            if (settings.value.trackAndReportLocation) {
                trackingCoordinator.setTrackingEnabled(true)
            }
        }
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setServerUrl(url)
        }
    }

    fun setEnableLocationHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEnableLocationHistory(enabled)
        }
    }

    fun setAllowedDeviceListEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowedDeviceListEnabled(enabled)
        }
    }

    fun setMapProvider(mapProvider: MapProvider) {
        viewModelScope.launch {
            settingsRepository.setMapProvider(mapProvider)
        }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            onComplete()
        }
    }
}
