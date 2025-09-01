# Implementierung eines API Clients in Kotlin
_Exported on 1.9.2025 at 22:33:02 MESZ from Cursor (1.5.7)_

---

**User**

verwende diese YAML API Definition und erstelle eine kotlin/android implementierung eines API Clients "MiataruAndroidClient". Die Library soll später für ein Android 15 Projekt mit Android Studio eingesetzt werden können

---

**Cursor**

Ich lese zuerst die `Miataru.yaml`, um die Endpunkte und Schemas zu extrahieren. Danach lege ich eine Android-Library-Struktur an und implementiere den `MiataruAndroidClient` mit Retrofit/OkHttp und JSON-Serialisierung.

I'll create an Android library module with Gradle files, models from the YAML, a Retrofit service, and the `MiataruAndroidClient` wrapper. Then I'll add a README with usage.

I'll scaffold a Gradle Android library project, then add the Retrofit/Moshi service, Kotlin models from the YAML, and the `MiataruAndroidClient`.

```plaintext
*** Begin Patch
*** Add File: /home/bietiekay/Downloads/untitled folder/settings.gradle.kts
pluginManagement {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}
	plugins {
		id("com.android.library") version "8.5.2"
		id("org.jetbrains.kotlin.android") version "1.9.24"
	}
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

rootProject.name = "miataru-android-client"
include(":miataru-client")
*** End Patch
```

I'll scaffold the Gradle Android library and module files, then add the data models and Retrofit service interfaces based on the YAML, followed by the client wrapper.

```plaintext
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.library") version "8.5.2"
        id("org.jetbrains.kotlin.android") version "1.9.24"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "miataru-android-client"
include(":miataru-client")
```

```plaintext
// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    // Versions are managed via settings.gradle.kts pluginManagement
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
kotlin.incremental=true
android.nonTransitiveRClass=true
android.enableJetifier=false
```

```plaintext
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.miataru.client"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xjvm-default=all")
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Moshi
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
}
```

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application />
</manifest>
```

```plaintext
data class MiataruGetLocationRequest(val MiataruConfig: MiataruConfig? = null, val MiataruGetLocation: List<MiataruGetLocationDevice>)
```

I'll add Kotlin models, Retrofit service, the `MiataruAndroidClient` wrapper, ProGuard files, and a README, then mark scaffolding tasks done and move on to models.

```plaintext
package com.miataru.client.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Core models

@JsonClass(generateAdapter = true)
data class Ack(
    @Json(name = "MiataruResponse") val miataruResponse: String,
    @Json(name = "MiataruVerboseResponse") val miataruVerboseResponse: String? = null
)

@JsonClass(generateAdapter = true)
data class MiataruLocation(
    @Json(name = "Device") val device: String,
    @Json(name = "Timestamp") val timestamp: String,
    @Json(name = "Longitude") val longitude: String,
    @Json(name = "Latitude") val latitude: String,
    @Json(name = "HorizontalAccuracy") val horizontalAccuracy: String
)

// UpdateLocation request
@JsonClass(generateAdapter = true)
data class UpdateConfig(
    @Json(name = "EnableLocationHistory") val enableLocationHistory: String,
    @Json(name = "LocationDataRetentionTime") val locationDataRetentionTime: String
)

@JsonClass(generateAdapter = true)
data class MiataruUpdateLocationRequest(
    @Json(name = "MiataruConfig") val miataruConfig: UpdateConfig,
    @Json(name = "MiataruLocation") val miataruLocation: List<MiataruLocation>
)

// GetLocation request
@JsonClass(generateAdapter = true)
data class RequestConfig(
    @Json(name = "RequestMiataruDeviceID") val requestMiataruDeviceID: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationDevice(
    @Json(name = "Device") val device: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationRequest(
    @Json(name = "MiataruConfig") val miataruConfig: RequestConfig? = null,
    @Json(name = "MiataruGetLocation") val miataruGetLocation: List<MiataruGetLocationDevice>
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationResponse(
    @Json(name = "MiataruLocation") val miataruLocation: List<MiataruLocation>
)

// GetLocationHistory request/response
@JsonClass(generateAdapter = true)
data class GetLocationHistoryPayload(
    @Json(name = "Device") val device: String,
    @Json(name = "Amount") val amount: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationHistoryRequest(
    @Json(name = "MiataruConfig") val miataruConfig: RequestConfig? = null,
    @Json(name = "MiataruGetLocationHistory") val miataruGetLocationHistory: GetLocationHistoryPayload
)

@JsonClass(generateAdapter = true)
data class LocationHistoryServerConfig(
    @Json(name = "MaximumNumberOfLocationUpdates") val maximumNumberOfLocationUpdates: String,
    @Json(name = "AvailableDeviceLocationUpdates") val availableDeviceLocationUpdates: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationHistoryResponse(
    @Json(name = "MiataruServerConfig") val miataruServerConfig: LocationHistoryServerConfig,
    @Json(name = "MiataruLocation") val miataruLocation: List<MiataruLocation>
)

// GetVisitorHistory request/response
@JsonClass(generateAdapter = true)
data class GetVisitorHistoryPayload(
    @Json(name = "Device") val device: String,
    @Json(name = "Amount") val amount: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetVisitorHistoryRequest(
    @Json(name = "MiataruGetVisitorHistory") val miataruGetVisitorHistory: GetVisitorHistoryPayload
)

@JsonClass(generateAdapter = true)
data class MiataruVisitor(
    @Json(name = "DeviceID") val deviceId: String,
    @Json(name = "TimeStamp") val timeStamp: String
)

@JsonClass(generateAdapter = true)
data class VisitorHistoryServerConfig(
    @Json(name = "MaximumNumberOfVisitorHistory") val maximumNumberOfVisitorHistory: String,
    @Json(name = "AvailableVisitorHistory") val availableVisitorHistory: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetVisitorHistoryResponse(
    @Json(name = "MiataruServerConfig") val miataruServerConfig: VisitorHistoryServerConfig,
    @Json(name = "MiataruVisitors") val miataruVisitors: List<MiataruVisitor>
)

// GeoJSON response
@JsonClass(generateAdapter = true)
data class GeoJsonGeometry(
    @Json(name = "type") val type: String,
    @Json(name = "coordinates") val coordinates: List<Double>
)

@JsonClass(generateAdapter = true)
data class GeoJsonProperties(
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class MiataruGetLocationGeoJSONResponse(
    @Json(name = "geometry") val geometry: GeoJsonGeometry,
    @Json(name = "type") val type: String,
    @Json(name = "properties") val properties: GeoJsonProperties
)
```

```plaintext
package com.miataru.client.network

import com.miataru.client.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface MiataruService {
    @Headers("Content-Type: application/json")
    @POST("UpdateLocation")
    suspend fun updateLocation(@Body body: MiataruUpdateLocationRequest): Ack

    @Headers("Content-Type: application/json")
    @POST("GetLocation")
    suspend fun getLocation(@Body body: MiataruGetLocationRequest): MiataruGetLocationResponse

    @GET("GetLocationGeoJSON/{deviceID}")
    suspend fun getLocationGeoJSON(@Path("deviceID") deviceId: String): MiataruGetLocationGeoJSONResponse

    @Headers("Content-Type: application/json")
    @POST("GetLocationHistory")
    suspend fun getLocationHistory(@Body body: MiataruGetLocationHistoryRequest): MiataruGetLocationHistoryResponse

    @Headers("Content-Type: application/json")
    @POST("GetVisitorHistory")
    suspend fun getVisitorHistory(@Body body: MiataruGetVisitorHistoryRequest): MiataruGetVisitorHistoryResponse
}
```

```plaintext
package com.miataru.client

import com.miataru.client.model.*
import com.miataru.client.network.MiataruService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class MiataruAndroidClient private constructor(
    private val service: MiataruService
) {

    suspend fun updateLocation(body: MiataruUpdateLocationRequest): Ack = service.updateLocation(body)

    suspend fun getLocation(body: MiataruGetLocationRequest): MiataruGetLocationResponse = service.getLocation(body)

    suspend fun getLocationGeoJSON(deviceId: String): MiataruGetLocationGeoJSONResponse = service.getLocationGeoJSON(deviceId)

    suspend fun getLocationHistory(body: MiataruGetLocationHistoryRequest): MiataruGetLocationHistoryResponse = service.getLocationHistory(body)

    suspend fun getVisitorHistory(body: MiataruGetVisitorHistoryRequest): MiataruGetVisitorHistoryResponse = service.getVisitorHistory(body)

    class Builder {
        private var baseUrl: String = DEFAULT_BASE_URL
        private var connectTimeoutSeconds: Long = 15
        private var readTimeoutSeconds: Long = 30
        private var writeTimeoutSeconds: Long = 30
        private var enableHttpLogging: Boolean = false
        private val networkInterceptors: MutableList<Interceptor> = mutableListOf()
        private val applicationInterceptors: MutableList<Interceptor> = mutableListOf()

        fun baseUrl(url: String) = apply { this.baseUrl = url }
        fun timeouts(connect: Long = 15, read: Long = 30, write: Long = 30) = apply {
            this.connectTimeoutSeconds = connect
            this.readTimeoutSeconds = read
            this.writeTimeoutSeconds = write
        }
        fun enableLogging(enable: Boolean) = apply { this.enableHttpLogging = enable }
        fun addNetworkInterceptor(interceptor: Interceptor) = apply { this.networkInterceptors.add(interceptor) }
        fun addApplicationInterceptor(interceptor: Interceptor) = apply { this.applicationInterceptors.add(interceptor) }

        fun build(): MiataruAndroidClient {
            val moshi: Moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val okHttpBuilder = OkHttpClient.Builder()
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)

            if (enableHttpLogging) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                okHttpBuilder.addInterceptor(logging)
            }

            applicationInterceptors.forEach { okHttpBuilder.addInterceptor(it) }
            networkInterceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(ensureEndsWithSlash(baseUrl))
                .client(okHttpBuilder.build())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(MiataruService::class.java)
            return MiataruAndroidClient(service)
        }

        private fun ensureEndsWithSlash(url: String): String = if (url.endsWith('/')) url else "$url/"
    }

    companion object {
        private const val DEFAULT_BASE_URL: String = "https://service.miataru.com/v1/"
    }
}
```

```plaintext
# ProGuard rules for Miataru client (library build)
# Keep Moshi generated adapters and Json-annotated classes
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn okio.**
```

```plaintext
# Consumer rules to keep Moshi/Kotlin reflection metadata for model classes
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn okio.**
```

```markdown
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
```

I created the Gradle project and module, and added models, service, client, and docs. I'll mark scaffolding as done and start implementing models/service/client tasks as completed next, then check for lints.

- Ich habe die Library `:miataru-client` als Android-15-kompatibles Modul erstellt, inkl. Gradle-Konfiguration, Modelle, Retrofit-Service und `MiataruAndroidClient`.
- Wichtigste Dateien:
  - `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`
  - `miataru-client/build.gradle.kts`
  - `miataru-client/src/main/java/com/miataru/client/MiataruAndroidClient.kt`
  - `miataru-client/src/main/java/com/miataru/client/network/MiataruService.kt`
  - `miataru-client/src/main/java/com/miataru/client/model/Models.kt`
  - ProGuard: `proguard-rules.pro`, `consumer-rules.pro`
  - `README.md` mit Usage.

Kurzbenutzung:
```kotlin
val client = MiataruAndroidClient.Builder()
  .baseUrl("https://service.miataru.com/v1/") // optional, ist Default
  .enableLogging(true)
  .build()

// Beispiel: UpdateLocation
val req = MiataruUpdateLocationRequest(
  miataruConfig = UpdateConfig("False", "30"),
  miataruLocation = listOf(
    MiataruLocation("device-id", "1441360863", "-4.394531", "41.079351", "50")
  )
)
// val ack = client.updateLocation(req)
```

Wenn du möchtest, kann ich noch Unit-Tests (Robolectric/MockWebServer), KDocs, und eine optionale Swift/KMP-Variante ergänzen.

```plaintext
val client = MiataruAndroidClient.Builder()
  .baseUrl("https://service.miataru.com/v1/") // optional, ist Default
  .enableLogging(true)
  .build()

// Beispiel: UpdateLocation
val req = MiataruUpdateLocationRequest(
  miataruConfig = UpdateConfig("False", "30"),
  miataruLocation = listOf(
    MiataruLocation("device-id", "1441360863", "-4.394531", "41.079351", "50")
  )
)
// val ack = client.updateLocation(req)
```

