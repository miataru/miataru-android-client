package com.miataru.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitor_events")
data class VisitorEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "visitor_device_id")
    val visitorDeviceId: String,
    @ColumnInfo(name = "timestamp_epoch_ms")
    val timestampEpochMs: Long,
    val ignored: Boolean,
)
