package com.miataru.domain.service

import com.miataru.domain.model.Device

interface AclSyncService {
    suspend fun syncAllowedDeviceList(): Result<Unit>
    suspend fun applyDeviceAccessChange(
        device: Device,
        hasCurrentLocationAccess: Boolean,
        hasLocationHistoryAccess: Boolean,
    ): Result<Device>
}
