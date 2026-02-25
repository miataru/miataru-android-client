package com.miataru.background

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.miataru.R
import com.miataru.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class TrackingForegroundService : Service() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var workManager: WorkManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var lastUploadedLocation: Location? = null
    private var lastUploadTimestampMs: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = runCatching { buildNotification() }.getOrElse { error ->
            Log.e(TAG, "Failed to create tracking notification.", error)
            stopSelf(startId)
            return START_NOT_STICKY
        }

        val started = runCatching {
            startForeground(
                TrackingWorkConstants.FOREGROUND_NOTIFICATION_ID,
                notification,
            )
        }

        if (started.isFailure) {
            Log.e(TAG, "startForeground failed for tracking service.", started.exceptionOrNull())
            stopSelf(startId)
            return START_NOT_STICKY
        }

        startLocationUpdatesIfPossible()
        serviceScope.launch { enqueueImmediateUpload(location = null) }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startLocationUpdatesIfPossible() {
        if (locationListener != null) {
            return
        }
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission missing; skipping live tracking updates.")
            return
        }

        val manager = getSystemService(LocationManager::class.java)
        if (manager == null) {
            Log.e(TAG, "LocationManager unavailable.")
            return
        }
        locationManager = manager

        val listener = LocationListener { location ->
            Log.d(TAG, "Location callback: lat=${location.latitude}, lon=${location.longitude}, provider=${location.provider}")
            if (shouldUpload(location)) {
                serviceScope.launch {
                    enqueueImmediateUpload(location)
                }
            }
        }
        locationListener = listener

        var anyProviderRegistered = false
        TRACKING_PROVIDERS.forEach { provider ->
            val registered = runCatching {
                manager.requestLocationUpdates(
                    provider,
                    CALLBACK_MIN_TIME_MS,
                    CALLBACK_MIN_DISTANCE_METERS,
                    listener,
                    Looper.getMainLooper(),
                )
            }
            if (registered.isSuccess) {
                anyProviderRegistered = true
            } else {
                Log.w(TAG, "Failed to register provider=$provider", registered.exceptionOrNull())
            }
        }

        if (!anyProviderRegistered) {
            Log.e(TAG, "No location provider could be registered for tracking.")
            return
        }

        // Bootstrap with newest known location so tracking can recover quickly after process/service restarts.
        val newestKnown = TRACKING_PROVIDERS
            .mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }
        if (newestKnown != null && shouldUpload(newestKnown)) {
            serviceScope.launch { enqueueImmediateUpload(newestKnown) }
        }
    }

    private fun stopLocationUpdates() {
        val manager = locationManager ?: return
        val listener = locationListener ?: return
        runCatching {
            manager.removeUpdates(listener)
        }.onFailure { error ->
            Log.w(TAG, "Failed to remove location updates.", error)
        }
        locationListener = null
        locationManager = null
    }

    private fun shouldUpload(location: Location): Boolean {
        val now = System.currentTimeMillis()
        val sinceLastUpload = now - lastUploadTimestampMs
        if (sinceLastUpload < UPLOAD_THROTTLE_MS) {
            return false
        }

        val previous = lastUploadedLocation
        val movedMeters = if (previous != null) location.distanceTo(previous) else Float.MAX_VALUE
        val accuracy = if (location.hasAccuracy()) location.accuracy else DEFAULT_ACCURACY_METERS
        val requiredDistanceMeters = max(SIGNIFICANT_CHANGE_DISTANCE_METERS, accuracy * ACCURACY_DISTANCE_FACTOR)
        if (previous != null &&
            movedMeters < requiredDistanceMeters &&
            sinceLastUpload < FORCE_UPLOAD_INTERVAL_MS
        ) {
            return false
        }

        lastUploadedLocation = Location(location)
        lastUploadTimestampMs = now
        return true
    }

    private suspend fun enqueueImmediateUpload(location: Location?) {
        settingsRepository.ensureOwnDeviceId()
        val settings = settingsRepository.currentSettings()
        if (settings.serverUrl.isBlank() || settings.ownDeviceId.isBlank()) {
            Log.w(TAG, "Skipping upload: missing serverUrl or ownDeviceId.")
            return
        }

        val inputData = Data.Builder()
            .putString(TrackingWorkConstants.INPUT_SERVER_URL, settings.serverUrl)
            .putString(TrackingWorkConstants.INPUT_OWN_DEVICE_ID, settings.ownDeviceId)
            .putString(TrackingWorkConstants.INPUT_OWN_DEVICE_KEY, settings.ownDeviceKey)
            .putBoolean(TrackingWorkConstants.INPUT_ENABLE_HISTORY, settings.enableLocationHistory)
            .apply {
                if (location != null) {
                    putDouble(TrackingWorkConstants.INPUT_LOCATION_LATITUDE, location.latitude)
                    putDouble(TrackingWorkConstants.INPUT_LOCATION_LONGITUDE, location.longitude)
                    if (location.hasAccuracy()) {
                        putFloat(TrackingWorkConstants.INPUT_LOCATION_ACCURACY, location.accuracy)
                    }
                    if (location.hasSpeed()) {
                        putFloat(TrackingWorkConstants.INPUT_LOCATION_SPEED, location.speed)
                    }
                    if (location.hasAltitude()) {
                        putDouble(TrackingWorkConstants.INPUT_LOCATION_ALTITUDE, location.altitude)
                    }
                    putLong(
                        TrackingWorkConstants.INPUT_LOCATION_TIMESTAMP_MS,
                        if (location.time > 0L) location.time else System.currentTimeMillis(),
                    )
                }
            }
            .build()

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
        Log.d(
            TAG,
            "Enqueued immediate upload (hasLocation=${location != null}, ownDeviceId=${settings.ownDeviceId.take(8)}...)",
        )
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, TrackingWorkConstants.FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.tracking_notification_title))
            .setContentText(getString(R.string.tracking_notification_message))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(TrackingWorkConstants.FOREGROUND_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    TrackingWorkConstants.FOREGROUND_CHANNEL_ID,
                    getString(R.string.tracking_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                )
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    companion object {
        private const val TAG = "TrackingFgService"
        private const val CALLBACK_MIN_TIME_MS = 10_000L
        private const val CALLBACK_MIN_DISTANCE_METERS = 50f
        private const val SIGNIFICANT_CHANGE_DISTANCE_METERS = 80f
        private const val DEFAULT_ACCURACY_METERS = 80f
        private const val ACCURACY_DISTANCE_FACTOR = 0.75f
        private const val UPLOAD_THROTTLE_MS = 10_000L
        private const val FORCE_UPLOAD_INTERVAL_MS = 3 * 60_000L
        private val TRACKING_PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}
