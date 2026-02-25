package com.miataru.data.service

import com.miataru.data.remote.AllowedDevicePolicy
import com.miataru.data.remote.GatewayResult
import com.miataru.data.remote.MiataruGateway
import com.miataru.domain.model.Device
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.service.AclSyncService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AclSyncServiceImpl @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val settingsRepository: SettingsRepository,
    private val miataruGateway: MiataruGateway,
) : AclSyncService {

    override suspend fun syncAllowedDeviceList(): Result<Unit> {
        val settings = settingsRepository.currentSettings()
        if (!settings.allowedDeviceListEnabled) {
            return Result.success(Unit)
        }

        if (settings.ownDeviceKey.isBlank()) {
            return Result.failure(IllegalStateException("Own device key is required for ACL sync"))
        }

        val devices = deviceRepository.devices.first()
        val allowedPolicies = devices
            .filter { !it.isOwnDevice }
            .filter { it.hasCurrentLocationAccess || it.hasLocationHistoryAccess }
            .map { device ->
                AllowedDevicePolicy(
                    deviceId = device.deviceId,
                    hasCurrentLocationAccess = device.hasCurrentLocationAccess,
                    hasLocationHistoryAccess = device.hasLocationHistoryAccess,
                )
            }

        return when (val response = miataruGateway.setAllowedDeviceList(
            ownDeviceId = settings.ownDeviceId,
            ownDeviceKey = settings.ownDeviceKey,
            allowedDevices = allowedPolicies,
        )) {
            is GatewayResult.Success -> Result.success(Unit)
            is GatewayResult.Error -> Result.failure(IllegalStateException(response.message, response.cause))
        }
    }

    override suspend fun applyDeviceAccessChange(
        device: Device,
        hasCurrentLocationAccess: Boolean,
        hasLocationHistoryAccess: Boolean,
    ): Result<Device> {
        val previous = device
        val updated = device.copy(
            hasCurrentLocationAccess = hasCurrentLocationAccess,
            hasLocationHistoryAccess = hasLocationHistoryAccess,
        )

        val localUpdate = deviceRepository.updateDevice(updated)
        if (localUpdate.isFailure) {
            return localUpdate
        }

        val syncResult = syncAllowedDeviceList()
        if (syncResult.isFailure) {
            deviceRepository.updateDevice(previous)
            return Result.failure(syncResult.exceptionOrNull() ?: IllegalStateException("ACL sync failed"))
        }

        return Result.success(updated)
    }
}
