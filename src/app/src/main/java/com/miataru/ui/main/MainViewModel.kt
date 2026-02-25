package com.miataru.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.data.service.DeepLinkResolution
import com.miataru.data.service.DeepLinkRouter
import com.miataru.domain.model.SettingsState
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.service.TrackingCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NavigationEvent {
    data class OpenDeviceMap(val deviceId: String) : NavigationEvent
    data class OpenAddDevice(val prefilledDeviceId: String) : NavigationEvent
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val deviceRepository: DeviceRepository,
    private val deepLinkRouter: DeepLinkRouter,
    private val trackingCoordinator: TrackingCoordinator,
) : ViewModel() {

    val settings: StateFlow<SettingsState> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsState(),
        )

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.ensureOwnDeviceId()
            deviceRepository.ensureOwnDeviceExists()

            // Bring tracking runtime state in sync with persisted setting on every app start.
            val current = settingsRepository.currentSettings()
            if (current.trackAndReportLocation) {
                trackingCoordinator.setTrackingEnabled(true)
            }
        }
    }

    fun handleDeepLink(uri: Uri?) {
        viewModelScope.launch {
            when (val resolution = deepLinkRouter.resolve(uri)) {
                DeepLinkResolution.Ignore -> Unit
                is DeepLinkResolution.OpenAddDevice -> {
                    _navigationEvents.emit(NavigationEvent.OpenAddDevice(resolution.prefilledDeviceId))
                }
                is DeepLinkResolution.OpenDeviceMap -> {
                    _navigationEvents.emit(NavigationEvent.OpenDeviceMap(resolution.deviceId))
                }
            }
        }
    }
}
