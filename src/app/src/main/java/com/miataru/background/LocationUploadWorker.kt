package com.miataru.background

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.miataru.client.MiataruAndroidClient
import com.miataru.client.model.MiataruLocation
import com.miataru.client.model.MiataruUpdateLocationRequest
import com.miataru.client.model.UpdateConfig
import com.miataru.util.toMiataruTimestampString

class LocationUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val hasFine = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            Log.w(TAG, "Skipping location upload: location permission missing.")
            return Result.retry()
        }

        val serverUrl = inputData.getString(TrackingWorkConstants.INPUT_SERVER_URL).orEmpty()
        val ownDeviceId = inputData.getString(TrackingWorkConstants.INPUT_OWN_DEVICE_ID).orEmpty()
        val ownDeviceKey = inputData.getString(TrackingWorkConstants.INPUT_OWN_DEVICE_KEY).orEmpty()
        val enableHistory = inputData.getBoolean(TrackingWorkConstants.INPUT_ENABLE_HISTORY, false)

        if (serverUrl.isBlank() || ownDeviceId.isBlank()) {
            Log.e(TAG, "Skipping location upload: missing serverUrl or ownDeviceId.")
            return Result.failure()
        }

        val location = locationFromInputData() ?: getBestLastKnownLocation() ?: return Result.retry()

        return runCatching {
            val client = MiataruAndroidClient.Builder()
                .baseUrl(serverUrl)
                .enableLogging(false)
                .build()

            val request = MiataruUpdateLocationRequest(
                miataruConfig = UpdateConfig(
                    enableLocationHistory = if (enableHistory) "True" else "False",
                    locationDataRetentionTime = "100",
                ),
                miataruLocation = listOf(
                    MiataruLocation(
                        device = ownDeviceId,
                        deviceKey = ownDeviceKey.ifBlank { null },
                        timestamp = toMiataruTimestampString(
                            if (location.time > 0) location.time else System.currentTimeMillis()
                        ),
                        longitude = location.longitude.toString(),
                        latitude = location.latitude.toString(),
                        horizontalAccuracy = location.accuracy.toString(),
                        speed = location.speed.toString(),
                        altitude = location.altitude.toString(),
                    )
                ),
            )

            client.updateLocation(request)
        }.fold(
            onSuccess = {
                Log.d(TAG, "Location upload successful for device=${ownDeviceId.take(8)}...")
                Result.success()
            },
            onFailure = {
                Log.w(TAG, "Location upload failed, will retry.", it)
                Result.retry()
            },
        )
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(): Location? {
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )

        var newest: Location? = null
        providers.forEach { provider ->
            val known = runCatching {
                locationManager.getLastKnownLocation(provider)
            }.getOrNull() ?: return@forEach

            if (newest == null || known.time > newest?.time ?: 0L) {
                newest = known
            }
        }

        return newest
    }

    private fun locationFromInputData(): Location? {
        val latitude = inputData.getDouble(TrackingWorkConstants.INPUT_LOCATION_LATITUDE, Double.NaN)
        val longitude = inputData.getDouble(TrackingWorkConstants.INPUT_LOCATION_LONGITUDE, Double.NaN)
        if (latitude.isNaN() || longitude.isNaN()) {
            return null
        }

        return Location("miataru_input").apply {
            this.latitude = latitude
            this.longitude = longitude

            val timestampMs = inputData.getLong(TrackingWorkConstants.INPUT_LOCATION_TIMESTAMP_MS, 0L)
            if (timestampMs > 0L) {
                this.time = timestampMs
            }

            val accuracy = inputData.getFloat(TrackingWorkConstants.INPUT_LOCATION_ACCURACY, -1f)
            if (accuracy >= 0f) {
                this.accuracy = accuracy
            }

            val speed = inputData.getFloat(TrackingWorkConstants.INPUT_LOCATION_SPEED, -1f)
            if (speed >= 0f) {
                this.speed = speed
            }

            val altitude = inputData.getDouble(TrackingWorkConstants.INPUT_LOCATION_ALTITUDE, Double.NaN)
            if (!altitude.isNaN()) {
                this.altitude = altitude
            }
        }
    }

    companion object {
        private const val TAG = "LocationUploadWorker"
    }
}
