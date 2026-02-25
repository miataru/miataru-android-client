package com.miataru.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.domain.model.DeviceWithLocation
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.VisitorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevicesUiState(
    val devices: List<DeviceWithLocation> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val visitorRepository: VisitorRepository,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DevicesUiState> = combine(
        deviceRepository.devicesWithLocation,
        refreshing,
        error,
    ) { devices, isRefreshing, currentError ->
        DevicesUiState(
            devices = devices,
            isRefreshing = isRefreshing,
            errorMessage = currentError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DevicesUiState(),
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            error.value = null

            val locationResult = deviceRepository.refreshLocations()
            val visitorResult = visitorRepository.refreshVisitorHistory()

            if (locationResult.isFailure) {
                error.value = locationResult.exceptionOrNull()?.message ?: "Failed to refresh device locations"
            } else if (visitorResult.isFailure) {
                error.value = visitorResult.exceptionOrNull()?.message ?: "Failed to refresh visitor history"
            }

            refreshing.value = false
        }
    }

    fun deleteDevice(deviceId: Long) {
        viewModelScope.launch {
            val result = deviceRepository.deleteDevice(deviceId)
            if (result.isFailure) {
                error.value = result.exceptionOrNull()?.message ?: "Failed to delete device"
            }
        }
    }

    fun clearError() {
        error.value = null
    }
}
