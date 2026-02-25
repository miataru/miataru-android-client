package com.miataru.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "group_memberships",
    primaryKeys = ["group_id", "device_id"],
    indices = [Index(value = ["group_id"]), Index(value = ["device_id"])]
)
data class GroupMembershipEntity(
    @ColumnInfo(name = "group_id")
    val groupId: Long,
    @ColumnInfo(name = "device_id")
    val deviceId: Long,
)
