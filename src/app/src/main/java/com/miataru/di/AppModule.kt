package com.miataru.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.miataru.data.local.MiataruDatabase
import com.miataru.data.local.dao.DeviceDao
import com.miataru.data.local.dao.GroupDao
import com.miataru.data.local.dao.GroupMembershipDao
import com.miataru.data.local.dao.LocationSnapshotDao
import com.miataru.data.local.dao.VisitorEventDao
import com.miataru.data.settings.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MiataruDatabase {
        return Room.databaseBuilder(
            context,
            MiataruDatabase::class.java,
            "miataru.db",
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideDeviceDao(database: MiataruDatabase): DeviceDao = database.deviceDao()

    @Provides
    fun provideGroupDao(database: MiataruDatabase): GroupDao = database.groupDao()

    @Provides
    fun provideGroupMembershipDao(database: MiataruDatabase): GroupMembershipDao = database.groupMembershipDao()

    @Provides
    fun provideLocationSnapshotDao(database: MiataruDatabase): LocationSnapshotDao = database.locationSnapshotDao()

    @Provides
    fun provideVisitorEventDao(database: MiataruDatabase): VisitorEventDao = database.visitorEventDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}
