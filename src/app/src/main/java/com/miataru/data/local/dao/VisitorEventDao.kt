package com.miataru.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miataru.data.local.entity.VisitorEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitorEventDao {
    @Query("SELECT * FROM visitor_events ORDER BY timestamp_epoch_ms DESC")
    fun observeAll(): Flow<List<VisitorEventEntity>>

    @Query("SELECT * FROM visitor_events WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): VisitorEventEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: VisitorEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<VisitorEventEntity>)

    @Update
    suspend fun update(entity: VisitorEventEntity)

    @Query("DELETE FROM visitor_events")
    suspend fun clearAll()
}
