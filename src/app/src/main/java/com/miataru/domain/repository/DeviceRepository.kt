package com.miataru.domain.repository

import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceWithLocation
import com.miataru.domain.model.LocationSnapshot
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    val devices: Flow<List<Device>>
    val devicesWithLocation: Flow<List<DeviceWithLocation>>

    suspend fun ensureOwnDeviceExists()
    suspend fun addDevice(device: Device): Result<Device>
    suspend fun updateDevice(device: Device): Result<Device>
    suspend fun deleteDevice(deviceId: Long): Result<Unit>
    suspend fun getById(deviceId: Long): Device?
    suspend fun getByDeviceId(deviceId: String): Device?
    fun observeLatestLocation(deviceId: String): Flow<LocationSnapshot?>
    suspend fun refreshLocations(targetDeviceIds: List<String> = emptyList()): Result<Unit>
}
