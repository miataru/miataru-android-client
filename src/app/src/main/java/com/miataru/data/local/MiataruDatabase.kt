package com.miataru.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miataru.data.local.dao.DeviceDao
import com.miataru.data.local.dao.GroupDao
import com.miataru.data.local.dao.GroupMembershipDao
import com.miataru.data.local.dao.LocationSnapshotDao
import com.miataru.data.local.dao.VisitorEventDao
import com.miataru.data.local.entity.DeviceEntity
import com.miataru.data.local.entity.DeviceGroupEntity
import com.miataru.data.local.entity.GroupMembershipEntity
import com.miataru.data.local.entity.LocationSnapshotEntity
import com.miataru.data.local.entity.VisitorEventEntity

@Database(
    entities = [
        DeviceEntity::class,
        DeviceGroupEntity::class,
        GroupMembershipEntity::class,
        LocationSnapshotEntity::class,
        VisitorEventEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MiataruDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMembershipDao(): GroupMembershipDao
    abstract fun locationSnapshotDao(): LocationSnapshotDao
    abstract fun visitorEventDao(): VisitorEventDao
}
