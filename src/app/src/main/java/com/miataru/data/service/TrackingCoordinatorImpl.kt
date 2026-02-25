package com.miataru.data.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.miataru.background.LocationUploadWorker
import com.miataru.background.TrackingForegroundService
import com.miataru.background.TrackingWorkConstants
import com.miataru.domain.repository.SettingsRepository
import com.miataru.domain.service.TrackingCoordinator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingCoordinatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val settingsRepository: SettingsRepository,
) : TrackingCoordinator {

    override val trackingEnabled: Flow<Boolean> = settingsRepository.settings.map { it.trackAndReportLocation }

    override suspend fun setTrackingEnabled(enabled: Boolean) {
        settingsRepository.setTrackAndReportLocation(enabled)
        if (enabled) {
            val ownDeviceEnsured = runCatching { settingsRepository.ensureOwnDeviceId() }
            if (ownDeviceEnsured.isFailure) {
                Log.e(TAG, "Failed to ensure own device ID for tracking.", ownDeviceEnsured.exceptionOrNull())
                return
            }

            if (!hasForegroundLocationPermission()) {
                Log.w(TAG, "Tracking enabled without location permission; deferring tracking start.")
                cancelTrackingWork()
                context.stopService(Intent(context, TrackingForegroundService::class.java))
                return
            }

            runCatching {
                schedulePeriodicUpload()
                startForegroundService()
                triggerImmediateUpload()
            }.onFailure { error ->
                Log.e(TAG, "Failed to start tracking stack.", error)
            }
        } else {
            cancelTrackingWork()
            context.stopService(Intent(context, TrackingForegroundService::class.java))
        }
    }

    override suspend fun triggerImmediateUpload() {
        val inputData = buildInputData()
        val work = OneTimeWorkRequestBuilder<LocationUploadWorker>()
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            TrackingWorkConstants.UNIQUE_IMMEDIATE_WORK,
            ExistingWorkPolicy.REPLACE,
            work,
        )
    }

    private suspend fun schedulePeriodicUpload() {
        val inputData = buildInputData()
        val periodic = PeriodicWorkRequestBuilder<LocationUploadWorker>(15, TimeUnit.MINUTES)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            TrackingWorkConstants.UNIQUE_PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic,
        )
    }

    private suspend fun buildInputData(): Data {
        val settings = settingsRepository.currentSettings()
        return Data.Builder()
            .putString(TrackingWorkConstants.INPUT_SERVER_URL, settings.serverUrl)
            .putString(TrackingWorkConstants.INPUT_OWN_DEVICE_ID, settings.ownDeviceId)
            .putString(TrackingWorkConstants.INPUT_OWN_DEVICE_KEY, settings.ownDeviceKey)
            .putBoolean(TrackingWorkConstants.INPUT_ENABLE_HISTORY, settings.enableLocationHistory)
            .build()
    }

    private fun startForegroundService() {
        runCatching {
            val intent = Intent(context, TrackingForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }.onFailure { error ->
            Log.e(TAG, "Unable to start foreground tracking service.", error)
        }
    }

    private fun hasForegroundLocationPermission(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return hasFine || hasCoarse
    }

    private fun cancelTrackingWork() {
        workManager.cancelUniqueWork(TrackingWorkConstants.UNIQUE_PERIODIC_WORK)
        workManager.cancelUniqueWork(TrackingWorkConstants.UNIQUE_IMMEDIATE_WORK)
    }

    companion object {
        private const val TAG = "TrackingCoordinator"
    }
}
