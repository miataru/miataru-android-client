package com.miataru.data.repository

import com.miataru.data.local.dao.VisitorEventDao
import com.miataru.data.mapper.toDomain
import com.miataru.data.mapper.toEntity
import com.miataru.data.remote.GatewayResult
import com.miataru.data.remote.MiataruGateway
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.repository.VisitorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitorRepositoryImpl @Inject constructor(
    private val visitorEventDao: VisitorEventDao,
    private val settingsRepository: SettingsRepository,
    private val miataruGateway: MiataruGateway,
) : VisitorRepository {

    override val visitors = visitorEventDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun refreshVisitorHistory(amount: Int): Result<Unit> {
        settingsRepository.ensureOwnDeviceId()
        val settings = settingsRepository.currentSettings()
        return when (val response = miataruGateway.getVisitorHistory(
            deviceId = settings.ownDeviceId,
            amount = amount,
            requestDeviceKey = settings.ownDeviceKey.ifBlank { null },
        )) {
            is GatewayResult.Success -> {
                visitorEventDao.insertAll(response.value.map { it.toEntity() })
                Result.success(Unit)
            }

            is GatewayResult.Error -> {
                Result.failure(IllegalStateException(response.message, response.cause))
            }
        }
    }

    override suspend fun setVisitorIgnored(visitorEventId: Long, ignored: Boolean): Result<Unit> {
        return runCatching {
            val current = visitorEventDao.getById(visitorEventId)
                ?: error("Visitor event not found")
            visitorEventDao.update(current.copy(ignored = ignored))
        }
    }

    override suspend fun clearAll(): Result<Unit> {
        return runCatching {
            visitorEventDao.clearAll()
        }
    }
}
