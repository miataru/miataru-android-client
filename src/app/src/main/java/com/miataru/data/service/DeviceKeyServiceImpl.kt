package com.miataru.data.service

import com.miataru.data.remote.GatewayResult
import com.miataru.data.remote.MiataruGateway
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.service.DeviceKeyService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceKeyServiceImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val miataruGateway: MiataruGateway,
) : DeviceKeyService {

    override suspend fun updateOwnDeviceKey(newDeviceKey: String): Result<Unit> {
        val settings = settingsRepository.currentSettings()
        if (newDeviceKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Device key must not be blank"))
        }

        return when (val response = miataruGateway.setDeviceKey(
            deviceId = settings.ownDeviceId,
            currentDeviceKey = settings.ownDeviceKey.ifBlank { null },
            newDeviceKey = newDeviceKey,
        )) {
            is GatewayResult.Success -> {
                settingsRepository.setOwnDeviceKey(newDeviceKey)
                Result.success(Unit)
            }

            is GatewayResult.Error -> {
                Result.failure(IllegalStateException(response.message, response.cause))
            }
        }
    }
}
