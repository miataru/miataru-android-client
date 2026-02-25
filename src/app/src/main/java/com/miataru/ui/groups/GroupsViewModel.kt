package com.miataru.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceGroup
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupsUiState(
    val groups: List<DeviceGroup> = emptyList(),
    val devices: List<Device> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GroupsUiState> = combine(
        groupRepository.groups,
        deviceRepository.devices,
        error,
    ) { groups, devices, errorMessage ->
        GroupsUiState(
            groups = groups,
            devices = devices,
            errorMessage = errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GroupsUiState(),
    )

    fun addGroup(name: String) {
        viewModelScope.launch {
            val result = groupRepository.addGroup(name.trim())
            if (result.isFailure) {
                error.value = result.exceptionOrNull()?.message ?: "Failed to add group"
            }
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            val result = groupRepository.deleteGroup(groupId)
            if (result.isFailure) {
                error.value = result.exceptionOrNull()?.message ?: "Failed to delete group"
            }
        }
    }

    fun updateGroupMembers(groupId: Long, deviceIds: List<Long>) {
        viewModelScope.launch {
            val result = groupRepository.updateGroupMembers(groupId, deviceIds)
            if (result.isFailure) {
                error.value = result.exceptionOrNull()?.message ?: "Failed to update group members"
            }
        }
    }

    fun clearError() {
        error.value = null
    }
}
