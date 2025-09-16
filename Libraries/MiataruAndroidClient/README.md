# Miataru Android Client

Android 15-ready Kotlin library for the Miataru API (`/v1`). Built with Retrofit, OkHttp, and Moshi.

## Install (as local module)

1. Copy the `miataru-client` module into your project (or use this root project directly).
2. Ensure your `settings.gradle` includes `:miataru-client`.

## Requirements
- Min SDK 24, Target/Compile SDK 35
- Kotlin 1.9.x, AGP 8.5+

## Usage

```kotlin
import com.miataru.client.MiataruAndroidClient
import com.miataru.client.model.*

val client = MiataruAndroidClient.Builder()
    .baseUrl("https://service.miataru.com/v1/") // default
    .enableLogging(true)
    .build()

// Update location
val update = MiataruUpdateLocationRequest(
    miataruConfig = UpdateConfig(
        enableLocationHistory = "False",
        locationDataRetentionTime = "30"
    ),
    miataruLocation = listOf(
        MiataruLocation(
            device = "device-id",
            timestamp = "1441360863",
            longitude = "-4.394531",
            latitude = "41.079351",
            horizontalAccuracy = "50"
        )
    )
)

// In a coroutine
// val ack = client.updateLocation(update)

// Get location
val getReq = MiataruGetLocationRequest(
    miataruConfig = RequestConfig(requestMiataruDeviceID = "requester-id"),
    miataruGetLocation = listOf(MiataruGetLocationDevice(device = "device-id"))
)
// val locations = client.getLocation(getReq)
```

## Endpoints covered
- POST `/UpdateLocation`
- POST `/GetLocation`
- GET `/GetLocationGeoJSON/{deviceID}`
- POST `/GetLocationHistory`
- POST `/GetVisitorHistory`

## Proguard/R8
Consumer rules are included to keep Moshi JSON adapters.
