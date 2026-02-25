package com.miataru.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.data.service.QrPayloadParseResult
import com.miataru.data.service.QrPayloadParser
import com.miataru.domain.model.Device
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.service.AclSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceEditorUiState(
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val deviceLocalId: Long? = null,
    val name: String = "",
    val deviceId: String = "",
    val colorHex: String = "#1E88E5",
    val deviceKey: String = "",
    val hasCurrentLocationAccess: Boolean = true,
    val hasLocationHistoryAccess: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface DeviceEditorEvent {
    data class Saved(val deviceId: String) : DeviceEditorEvent
}

@HiltViewModel
class DeviceEditorViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val aclSyncService: AclSyncService,
    private val qrPayloadParser: QrPayloadParser,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceEditorUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DeviceEditorEvent>()
    val events = _events.asSharedFlow()

    private var initializedWith: Pair<Long?, String?>? = null

    fun load(deviceLocalId: Long?, prefilledDeviceId: String?) {
        val key = deviceLocalId to prefilledDeviceId
        if (initializedWith == key) return
        initializedWith = key

        viewModelScope.launch {
            if (deviceLocalId != null) {
                val existing = deviceRepository.getById(deviceLocalId)
                if (existing == null) {
                    _uiState.update {
                        it.copy(
                            isLoaded = true,
                            errorMessage = "Device not found",
                        )
                    }
                    return@launch
                }

                _uiState.value = DeviceEditorUiState(
                    isLoaded = true,
                    isEditing = true,
                    deviceLocalId = existing.id,
                    name = existing.name,
                    deviceId = existing.deviceId,
                    colorHex = existing.colorHex,
                    deviceKey = existing.deviceKey.orEmpty(),
                    hasCurrentLocationAccess = existing.hasCurrentLocationAccess,
                    hasLocationHistoryAccess = existing.hasLocationHistoryAccess,
                )
            } else {
                _uiState.value = DeviceEditorUiState(
                    isLoaded = true,
                    isEditing = false,
                    deviceId = prefilledDeviceId.orEmpty(),
                )
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onDeviceIdChanged(value: String) {
        _uiState.update { it.copy(deviceId = value, errorMessage = null) }
    }

    fun onColorChanged(value: String) {
        _uiState.update { it.copy(colorHex = value, errorMessage = null) }
    }

    fun onDeviceKeyChanged(value: String) {
        _uiState.update { it.copy(deviceKey = value, errorMessage = null) }
    }

    fun onHasCurrentLocationAccessChanged(value: Boolean) {
        _uiState.update { it.copy(hasCurrentLocationAccess = value, errorMessage = null) }
    }

    fun onHasLocationHistoryAccessChanged(value: Boolean) {
        _uiState.update { it.copy(hasLocationHistoryAccess = value, errorMessage = null) }
    }

    fun applyQrPayload(payload: String) {
        when (val parseResult = qrPayloadParser.parse(payload)) {
            is QrPayloadParseResult.Invalid -> {
                _uiState.update { it.copy(errorMessage = parseResult.reason) }
            }

            is QrPayloadParseResult.Valid -> {
                _uiState.update {
                    it.copy(
                        deviceId = parseResult.deviceId,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    fun save() {
        val current = _uiState.value
        if (current.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required") }
            return
        }
        if (current.deviceId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Device ID is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val device = Device(
                id = current.deviceLocalId ?: 0,
                name = current.name.trim(),
                deviceId = current.deviceId.trim(),
                colorHex = current.colorHex.trim().ifBlank { "#1E88E5" },
                deviceKey = current.deviceKey.trim().ifBlank { null },
                hasCurrentLocationAccess = current.hasCurrentLocationAccess,
                hasLocationHistoryAccess = current.hasLocationHistoryAccess,
            )

            val writeResult = if (current.isEditing) {
                deviceRepository.updateDevice(device)
            } else {
                deviceRepository.addDevice(device)
            }

            if (writeResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = writeResult.exceptionOrNull()?.message ?: "Failed to save device",
                    )
                }
                return@launch
            }

            val aclResult = aclSyncService.syncAllowedDeviceList()
            if (aclResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = aclResult.exceptionOrNull()?.message ?: "ACL sync failed",
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = false, errorMessage = null) }
            _events.emit(DeviceEditorEvent.Saved(device.deviceId))
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
