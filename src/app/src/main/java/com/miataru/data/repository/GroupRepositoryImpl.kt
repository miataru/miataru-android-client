package com.miataru.data.repository

import com.miataru.data.local.dao.DeviceDao
import com.miataru.data.local.dao.GroupDao
import com.miataru.data.local.dao.GroupMembershipDao
import com.miataru.data.local.entity.GroupMembershipEntity
import com.miataru.data.mapper.toDomain
import com.miataru.data.mapper.toEntity
import com.miataru.domain.model.Device
import com.miataru.domain.model.DeviceGroup
import com.miataru.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val groupMembershipDao: GroupMembershipDao,
    private val deviceDao: DeviceDao,
) : GroupRepository {

    override val groups: Flow<List<DeviceGroup>> = groupDao.observeAll().flatMapLatest { entities ->
        if (entities.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(entities.map { entity ->
                groupMembershipDao.observeDeviceIdsByGroup(entity.id).map { memberIds ->
                    entity.toDomain(memberIds)
                }
            }) { groups ->
                groups.toList()
            }
        }
    }

    override suspend fun addGroup(name: String): Result<DeviceGroup> {
        return runCatching {
            val sortOrder = groupDao.nextSortOrder()
            val id = groupDao.insert(DeviceGroup(name = name, sortOrder = sortOrder).toEntity())
            DeviceGroup(id = id, name = name, sortOrder = sortOrder)
        }
    }

    override suspend fun updateGroup(group: DeviceGroup): Result<DeviceGroup> {
        return runCatching {
            val current = groupDao.getById(group.id)
                ?: error("Group not found")
            val updated = current.copy(name = group.name)
            groupDao.update(updated)
            group.copy(name = updated.name)
        }
    }

    override suspend fun deleteGroup(groupId: Long): Result<Unit> {
        return runCatching {
            val current = groupDao.getById(groupId) ?: return@runCatching
            groupMembershipDao.deleteByGroup(groupId)
            groupDao.delete(current)
        }
    }

    override suspend fun updateGroupMembers(groupId: Long, deviceIds: List<Long>): Result<Unit> {
        return runCatching {
            groupMembershipDao.deleteByGroup(groupId)
            groupMembershipDao.insertAll(deviceIds.map { deviceId ->
                GroupMembershipEntity(groupId = groupId, deviceId = deviceId)
            })
        }
    }

    override fun observeGroupDevices(groupId: Long): Flow<List<Device>> {
        return combine(
            deviceDao.observeAll(),
            groupMembershipDao.observeDeviceIdsByGroup(groupId),
        ) { devices, groupDeviceIds ->
            val memberSet = groupDeviceIds.toSet()
            devices.filter { it.id in memberSet }
                .map { it.toDomain() }
        }
    }
}
