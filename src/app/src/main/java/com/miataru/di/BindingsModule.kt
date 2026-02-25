package com.miataru.di

import com.miataru.data.remote.MiataruGateway
import com.miataru.data.remote.MiataruGatewayImpl
import com.miataru.data.repository.DeviceRepositoryImpl
import com.miataru.data.repository.GroupRepositoryImpl
import com.miataru.data.repository.SettingsRepositoryImpl
import com.miataru.data.repository.VisitorRepositoryImpl
import com.miataru.data.service.AclSyncServiceImpl
import com.miataru.data.service.DeviceKeyServiceImpl
import com.miataru.data.service.TrackingCoordinatorImpl
import com.miataru.domain.repository.DeviceRepository
import com.miataru.domain.repository.GroupRepository
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.repository.VisitorRepository
import com.miataru.domain.service.AclSyncService
import com.miataru.domain.service.DeviceKeyService
import com.miataru.domain.service.TrackingCoordinator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindVisitorRepository(impl: VisitorRepositoryImpl): VisitorRepository

    @Binds
    @Singleton
    abstract fun bindGateway(impl: MiataruGatewayImpl): MiataruGateway

    @Binds
    @Singleton
    abstract fun bindAclSyncService(impl: AclSyncServiceImpl): AclSyncService

    @Binds
    @Singleton
    abstract fun bindDeviceKeyService(impl: DeviceKeyServiceImpl): DeviceKeyService

    @Binds
    @Singleton
    abstract fun bindTrackingCoordinator(impl: TrackingCoordinatorImpl): TrackingCoordinator
}
