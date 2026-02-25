package com.miataru.domain.model

data class Device(
    val id: Long = 0,
    val name: String,
    val deviceId: String,
    val colorHex: String = "#1E88E5",
    val deviceKey: String? = null,
    val hasCurrentLocationAccess: Boolean = true,
    val hasLocationHistoryAccess: Boolean = false,
    val slogan: String? = null,
    val isOwnDevice: Boolean = false,
    val sortOrder: Int = 0,
)
