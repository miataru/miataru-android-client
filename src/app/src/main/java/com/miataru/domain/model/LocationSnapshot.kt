package com.miataru.domain.model

data class LocationSnapshot(
    val deviceId: String,
    val timestampEpochMs: Long,
    val latitude: Double,
    val longitude: Double,
    val horizontalAccuracyMeters: Double? = null,
    val speedMps: Double? = null,
    val batteryLevelPercent: Double? = null,
    val altitudeMeters: Double? = null,
)
