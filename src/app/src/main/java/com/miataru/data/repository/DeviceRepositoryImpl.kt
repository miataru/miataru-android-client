package com.miataru.data.repository

import com.miataru.data.local.dao.DeviceDao
import com.miataru.data.local.dao.GroupMembershipDao
import com.miataru.data.local.dao.LocationSnapshotDao
import com.miataru.data.mapper.toDomain
import com.miataru.data.mapper.toEntity
import com.miataru.data.remote.GatewayResult
import com.miataru.data.remote.MiataruGateway
import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceWithLocation
import com.miataru.domain.model.LocationSnapshot
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao,
    private val locationSnapshotDao: LocationSnapshotDao,
    private val groupMembershipDao: GroupMembershipDao,
    private val settingsRepository: SettingsRepository,
    private val miataruGateway: MiataruGateway,
) : DeviceRepository {

    override val devices: Flow<List<Device>> = deviceDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override val devicesWithLocation: Flow<List<DeviceWithLocation>> = devices.flatMapLatest { allDevices ->
        if (allDevices.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(allDevices.map { device ->
                observeLatestLocation(device.deviceId).map { latest ->
                    DeviceWithLocation(device = device, latestLocation = latest)
                }
            }) { items ->
                items.toList()
            }
        }
    }

    override suspend fun ensureOwnDeviceExists() {
        settingsRepository.ensureOwnDeviceId()
        val settings = settingsRepository.currentSettings()
        val existing = deviceDao.getByDeviceId(settings.ownDeviceId)
        if (existing == null) {
            val nextSortOrder = deviceDao.nextSortOrder()
            deviceDao.insert(
                Device(
                    name = "This Device",
                    deviceId = settings.ownDeviceId,
                    colorHex = "#00695C",
                    deviceKey = settings.ownDeviceKey.ifBlank { null },
                    isOwnDevice = true,
                    hasCurrentLocationAccess = true,
                    hasLocationHistoryAccess = true,
                    sortOrder = nextSortOrder,
                ).toEntity()
            )
        }
    }

    override suspend fun addDevice(device: Device): Result<Device> {
        return runCatching {
            val existing = deviceDao.getByDeviceId(device.deviceId)
            require(existing == null) { "Device with this device ID already exists" }
            val sortOrder = deviceDao.nextSortOrder()
            val id = deviceDao.insert(device.copy(sortOrder = sortOrder).toEntity())
            deviceDao.getById(id)?.toDomain()
                ?: error("Inserted device not found")
        }
    }

    override suspend fun updateDevice(device: Device): Result<Device> {
        return runCatching {
            val current = deviceDao.getById(device.id)
                ?: error("Device not found")

            val conflicting = deviceDao.getByDeviceId(device.deviceId)
            require(conflicting == null || conflicting.id == device.id) {
                "A different device already uses this device ID"
            }

            val updated = current.copy(
                name = device.name,
                deviceId = device.deviceId,
                colorHex = device.colorHex,
                deviceKey = device.deviceKey,
                hasCurrentLocationAccess = device.hasCurrentLocationAccess,
                hasLocationHistoryAccess = device.hasLocationHistoryAccess,
                slogan = device.slogan,
            )
            deviceDao.update(updated)
            updated.toDomain()
        }
    }

    override suspend fun deleteDevice(deviceId: Long): Result<Unit> {
        return runCatching {
            val current = deviceDao.getById(deviceId) ?: return@runCatching
            groupMembershipDao.deleteByDevice(deviceId)
            deviceDao.delete(current)
        }
    }

    override suspend fun getById(deviceId: Long): Device? = deviceDao.getById(deviceId)?.toDomain()

    override suspend fun getByDeviceId(deviceId: String): Device? = deviceDao.getByDeviceId(deviceId)?.toDomain()

    override fun observeLatestLocation(deviceId: String): Flow<LocationSnapshot?> {
        return locationSnapshotDao.observeLatestByDeviceId(deviceId).map { it?.toDomain() }
    }

    override suspend fun refreshLocations(targetDeviceIds: List<String>): Result<Unit> {
        settingsRepository.ensureOwnDeviceId()
        val settings = settingsRepository.currentSettings()
        val allDevices = deviceDao.getAll()
        val ids = if (targetDeviceIds.isEmpty()) {
            allDevices
                .filter { !it.isOwnDevice }
                .map { it.deviceId }
        } else {
            targetDeviceIds
        }

        if (ids.isEmpty()) {
            return Result.success(Unit)
        }

        return when (val response = miataruGateway.getLocations(
            deviceIds = ids,
            requestDeviceId = settings.ownDeviceId,
            requestDeviceKey = settings.ownDeviceKey.ifBlank { null },
        )) {
            is GatewayResult.Success -> {
                locationSnapshotDao.insertAll(response.value.map { it.toEntity() })
                Result.success(Unit)
            }

            is GatewayResult.Error -> {
                Result.failure(IllegalStateException(response.message, response.cause))
            }
        }
    }
}
