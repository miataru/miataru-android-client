package com.miataru.ui.devices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.ui.main.AppRoutes
import com.miataru.domain.model.LocationSnapshot
import com.miataru.domain.model.MapProvider
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceMapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deviceRepository: DeviceRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val deviceId: String = savedStateHandle.get<String>(AppRoutes.ARG_DEVICE_ID).orEmpty()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val mapProvider: StateFlow<MapProvider> = settingsRepository.settings
        .map { it.mapProvider }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapProvider.GOOGLE,
        )

    val location: StateFlow<LocationSnapshot?> = deviceRepository.observeLatestLocation(deviceId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun refresh() {
        viewModelScope.launch {
            val result = deviceRepository.refreshLocations(targetDeviceIds = listOf(deviceId))
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to refresh location"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
