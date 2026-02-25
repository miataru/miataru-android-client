package com.miataru.domain.repository

import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceGroup
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    val groups: Flow<List<DeviceGroup>>

    suspend fun addGroup(name: String): Result<DeviceGroup>
    suspend fun updateGroup(group: DeviceGroup): Result<DeviceGroup>
    suspend fun deleteGroup(groupId: Long): Result<Unit>
    suspend fun updateGroupMembers(groupId: Long, deviceIds: List<Long>): Result<Unit>
    fun observeGroupDevices(groupId: Long): Flow<List<Device>>
}
