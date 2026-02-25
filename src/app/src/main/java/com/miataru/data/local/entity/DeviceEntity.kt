package com.miataru.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    indices = [Index(value = ["device_id"], unique = true)]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    @ColumnInfo(name = "device_key")
    val deviceKey: String?,
    @ColumnInfo(name = "has_current_location_access")
    val hasCurrentLocationAccess: Boolean,
    @ColumnInfo(name = "has_location_history_access")
    val hasLocationHistoryAccess: Boolean,
    val slogan: String?,
    @ColumnInfo(name = "is_own_device")
    val isOwnDevice: Boolean,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)
