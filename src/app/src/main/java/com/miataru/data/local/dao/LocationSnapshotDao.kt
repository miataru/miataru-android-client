package com.miataru.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miataru.data.local.entity.LocationSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationSnapshotDao {
    @Query(
        """
        SELECT * FROM location_snapshots
        WHERE device_id = :deviceId
        ORDER BY timestamp_epoch_ms DESC
        LIMIT 1
        """
    )
    fun observeLatestByDeviceId(deviceId: String): Flow<LocationSnapshotEntity?>

    @Query(
        """
        SELECT ls.*
        FROM location_snapshots ls
        INNER JOIN (
            SELECT device_id, MAX(timestamp_epoch_ms) AS max_ts
            FROM location_snapshots
            GROUP BY device_id
        ) latest
        ON latest.device_id = ls.device_id
        AND latest.max_ts = ls.timestamp_epoch_ms
        WHERE ls.device_id IN (:deviceIds)
        """
    )
    fun observeLatestByDeviceIds(deviceIds: List<String>): Flow<List<LocationSnapshotEntity>>

    @Query(
        """
        SELECT * FROM location_snapshots
        WHERE device_id = :deviceId
        ORDER BY timestamp_epoch_ms DESC
        LIMIT :amount
        """
    )
    suspend fun getRecentByDeviceId(deviceId: String, amount: Int): List<LocationSnapshotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<LocationSnapshotEntity>)
}
