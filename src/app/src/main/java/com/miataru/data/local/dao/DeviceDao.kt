package com.miataru.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miataru.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY sort_order ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY sort_order ASC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: Long): DeviceEntity?

    @Query("SELECT * FROM devices WHERE device_id = :deviceId LIMIT 1")
    suspend fun getByDeviceId(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE is_own_device = 1 LIMIT 1")
    suspend fun getOwnDevice(): DeviceEntity?

    @Query("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM devices")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: DeviceEntity): Long

    @Update
    suspend fun update(entity: DeviceEntity)

    @Delete
    suspend fun delete(entity: DeviceEntity)
}
