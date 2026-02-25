package com.miataru.domain.service

interface DeviceKeyService {
    suspend fun updateOwnDeviceKey(newDeviceKey: String): Result<Unit>
}
