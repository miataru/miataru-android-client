package com.miataru.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.ui.main.AppRoutes
import com.miataru.domain.model.DeviceWithLocation
import com.miataru.domain.model.MapProvider
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.GroupRepository
import com.miataru.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupMapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val deviceRepository: DeviceRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val groupId: Long = savedStateHandle.get<Long>(AppRoutes.ARG_GROUP_ID) ?: -1L

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val mapProvider: StateFlow<MapProvider> = settingsRepository.settings
        .map { it.mapProvider }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapProvider.GOOGLE,
        )

    val devicesWithLocation: StateFlow<List<DeviceWithLocation>> = groupRepository.observeGroupDevices(groupId)
        .flatMapLatest { groupDevices ->
            if (groupDevices.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(groupDevices.map { device ->
                    deviceRepository.observeLatestLocation(device.deviceId).map { latest ->
                        DeviceWithLocation(device = device, latestLocation = latest)
                    }
                }) { rows ->
                    rows.toList()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun refresh() {
        viewModelScope.launch {
            val deviceIds = devicesWithLocation.value.map { it.device.deviceId }
            val result = deviceRepository.refreshLocations(deviceIds)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to refresh group locations"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
