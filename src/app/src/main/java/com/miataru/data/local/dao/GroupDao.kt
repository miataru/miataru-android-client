package com.miataru.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.miataru.data.local.entity.DeviceGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM device_groups ORDER BY sort_order ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<DeviceGroupEntity>>

    @Query("SELECT * FROM device_groups WHERE id = :groupId LIMIT 1")
    suspend fun getById(groupId: Long): DeviceGroupEntity?

    @Query("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM device_groups")
    suspend fun nextSortOrder(): Int

    @Insert
    suspend fun insert(entity: DeviceGroupEntity): Long

    @Update
    suspend fun update(entity: DeviceGroupEntity)

    @Delete
    suspend fun delete(entity: DeviceGroupEntity)
}
