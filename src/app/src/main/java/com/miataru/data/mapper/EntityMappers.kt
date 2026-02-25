package com.miataru.data.mapper

import com.miataru.data.local.entity.DeviceEntity
import com.miataru.data.local.entity.DeviceGroupEntity
import com.miataru.data.local.entity.LocationSnapshotEntity
import com.miataru.data.local.entity.VisitorEventEntity
import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceGroup
import com.miataru.domain.model.LocationSnapshot
import com.miataru.domain.model.VisitorEvent

fun DeviceEntity.toDomain(): Device = Device(
    id = id,
    name = name,
    deviceId = deviceId,
    colorHex = colorHex,
    deviceKey = deviceKey,
    hasCurrentLocationAccess = hasCurrentLocationAccess,
    hasLocationHistoryAccess = hasLocationHistoryAccess,
    slogan = slogan,
    isOwnDevice = isOwnDevice,
    sortOrder = sortOrder,
)

fun Device.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    name = name,
    deviceId = deviceId,
    colorHex = colorHex,
    deviceKey = deviceKey,
    hasCurrentLocationAccess = hasCurrentLocationAccess,
    hasLocationHistoryAccess = hasLocationHistoryAccess,
    slogan = slogan,
    isOwnDevice = isOwnDevice,
    sortOrder = sortOrder,
)

fun DeviceGroupEntity.toDomain(memberDeviceIds: List<Long>): DeviceGroup = DeviceGroup(
    id = id,
    name = name,
    sortOrder = sortOrder,
    memberDeviceIds = memberDeviceIds,
)

fun DeviceGroup.toEntity(): DeviceGroupEntity = DeviceGroupEntity(
    id = id,
    name = name,
    sortOrder = sortOrder,
)

fun LocationSnapshotEntity.toDomain(): LocationSnapshot = LocationSnapshot(
    deviceId = deviceId,
    timestampEpochMs = timestampEpochMs,
    latitude = latitude,
    longitude = longitude,
    horizontalAccuracyMeters = horizontalAccuracyMeters,
    speedMps = speedMps,
    batteryLevelPercent = batteryLevelPercent,
    altitudeMeters = altitudeMeters,
)

fun LocationSnapshot.toEntity(): LocationSnapshotEntity = LocationSnapshotEntity(
    deviceId = deviceId,
    timestampEpochMs = timestampEpochMs,
    latitude = latitude,
    longitude = longitude,
    horizontalAccuracyMeters = horizontalAccuracyMeters,
    speedMps = speedMps,
    batteryLevelPercent = batteryLevelPercent,
    altitudeMeters = altitudeMeters,
)

fun VisitorEventEntity.toDomain(): VisitorEvent = VisitorEvent(
    id = id,
    visitorDeviceId = visitorDeviceId,
    timestampEpochMs = timestampEpochMs,
    ignored = ignored,
)

fun VisitorEvent.toEntity(): VisitorEventEntity = VisitorEventEntity(
    id = id,
    visitorDeviceId = visitorDeviceId,
    timestampEpochMs = timestampEpochMs,
    ignored = ignored,
)
