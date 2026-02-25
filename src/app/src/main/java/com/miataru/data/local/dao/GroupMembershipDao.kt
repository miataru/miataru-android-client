package com.miataru.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.miataru.data.local.entity.GroupMembershipEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMembershipDao {
    @Query("SELECT device_id FROM group_memberships WHERE group_id = :groupId")
    fun observeDeviceIdsByGroup(groupId: Long): Flow<List<Long>>

    @Query("SELECT device_id FROM group_memberships WHERE group_id = :groupId")
    suspend fun getDeviceIdsByGroup(groupId: Long): List<Long>

    @Query("DELETE FROM group_memberships WHERE group_id = :groupId")
    suspend fun deleteByGroup(groupId: Long)

    @Query("DELETE FROM group_memberships WHERE device_id = :deviceId")
    suspend fun deleteByDevice(deviceId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memberships: List<GroupMembershipEntity>)
}
