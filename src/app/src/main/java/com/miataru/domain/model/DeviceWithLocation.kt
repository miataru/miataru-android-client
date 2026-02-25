package com.miataru.domain.model

data class DeviceWithLocation(
    val device: Device,
    val latestLocation: LocationSnapshot?,
)
