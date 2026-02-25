package com.miataru.domain.model

data class DeviceGroup(
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val memberDeviceIds: List<Long> = emptyList(),
)
