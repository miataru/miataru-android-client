package com.miataru.background

object TrackingWorkConstants {
    const val UNIQUE_PERIODIC_WORK = "miataru_location_upload_periodic"
    const val UNIQUE_IMMEDIATE_WORK = "miataru_location_upload_immediate"

    const val INPUT_SERVER_URL = "input_server_url"
    const val INPUT_OWN_DEVICE_ID = "input_own_device_id"
    const val INPUT_OWN_DEVICE_KEY = "input_own_device_key"
    const val INPUT_ENABLE_HISTORY = "input_enable_history"
    const val INPUT_LOCATION_LATITUDE = "input_location_latitude"
    const val INPUT_LOCATION_LONGITUDE = "input_location_longitude"
    const val INPUT_LOCATION_ACCURACY = "input_location_accuracy"
    const val INPUT_LOCATION_SPEED = "input_location_speed"
    const val INPUT_LOCATION_ALTITUDE = "input_location_altitude"
    const val INPUT_LOCATION_TIMESTAMP_MS = "input_location_timestamp_ms"

    const val FOREGROUND_CHANNEL_ID = "miataru_tracking_channel"
    const val FOREGROUND_NOTIFICATION_ID = 1101
}
