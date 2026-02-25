package com.miataru.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_snapshots",
    indices = [Index(value = ["device_id", "timestamp_epoch_ms"])]
)
data class LocationSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "timestamp_epoch_ms")
    val timestampEpochMs: Long,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "horizontal_accuracy_m")
    val horizontalAccuracyMeters: Double?,
    @ColumnInfo(name = "speed_mps")
    val speedMps: Double?,
    @ColumnInfo(name = "battery_level_percent")
    val batteryLevelPercent: Double?,
    @ColumnInfo(name = "altitude_m")
    val altitudeMeters: Double?,
)
