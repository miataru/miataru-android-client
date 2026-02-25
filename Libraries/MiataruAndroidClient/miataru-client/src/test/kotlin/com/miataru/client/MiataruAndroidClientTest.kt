package com.miataru.client

import com.miataru.client.model.Ack
import com.miataru.client.model.GetLocationHistoryPayload
import com.miataru.client.model.GetVisitorHistoryPayload
import com.miataru.client.model.MiataruDeleteLocationPayload
import com.miataru.client.model.MiataruDeleteLocationRequest
import com.miataru.client.model.MiataruDeleteLocationResponse
import com.miataru.client.model.MiataruGetLocationDevice
import com.miataru.client.model.MiataruGetLocationHistoryRequest
import com.miataru.client.model.MiataruGetLocationRequest
import com.miataru.client.model.MiataruGetLocationResponse
import com.miataru.client.model.MiataruLocation
import com.miataru.client.model.MiataruSetDeviceKeyPayload
import com.miataru.client.model.MiataruSetDeviceKeyRequest
import com.miataru.client.model.MiataruSetDeviceKeyResponse
import com.miataru.client.model.MiataruUpdateLocationRequest
import com.miataru.client.model.RequestConfig
import com.miataru.client.model.UpdateConfig
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * MockWebServer tests for MiataruAndroidClient: endpoints, request/response wiring, and builder.
 */
class MiataruAndroidClientTest {

    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun teardown() {
        server.shutdown()
    }

    private fun baseUrl(): String = server.url("/").toString()

    @Test
    fun builder_usesProvidedBaseUrl() = runTest {
        server.enqueue(MockResponse().setBody("""{"MiataruResponse":"ACK","MiataruVerboseResponse":""}""").setResponseCode(200))
        val client = MiataruAndroidClient.Builder().baseUrl(baseUrl()).build()
        val req = MiataruUpdateLocationRequest(
            miataruConfig = UpdateConfig(enableLocationHistory = "False", locationDataRetentionTime = "30"),
            miataruLocation = listOf(
                MiataruLocation(
                    device = "test-dev",
                    timestamp = "1441360863",
                    longitude = "-4.39",
                    latitude = "41.07",
                    horizontalAccuracy = "50"
                )
            )
        )
        val ack = client.updateLocation(req)
        assertEquals("ACK", ack.miataruResponse)
        val recorded = server.takeRequest(2, TimeUnit.SECONDS)
        assertNotNull(recorded)
        assertEquals("/UpdateLocation", recorded.path)
        assertEquals("POST", recorded.method)
    }

    @Test
    fun updateLocation_success() = runTest {
        server.enqueue(MockResponse().setBody("""{"MiataruResponse":"ACK","MiataruVerboseResponse":""}""").setResponseCode(200))
        val client = MiataruAndroidClient.Builder().baseUrl(baseUrl()).build()
        val req = MiataruUpdateLocationRequest(
            miataruConfig = UpdateConfig(enableLocationHistory = "False", locationDataRetentionTime = "30"),
            miataruLocation = listOf(
                MiataruLocation(
                    device = "dev-1",
                    timestamp = "1441360863",
                    longitude = "-4.394531",
                    latitude = "41.079351",
                    horizontalAccuracy = "50"
                )
            )
        )
        val ack = client.updateLocation(req)
        assertEquals("ACK", ack.miataruResponse)
    }

    @Test
    fun getLocation_success() = runTest {
        val body = """{"MiataruLocation":[{"Device":"dev-1","Timestamp":"1441360863","Longitude":"-4.39","Latitude":"41.07","HorizontalAccuracy":"50"}]}"""
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))
        val client = MiataruAndroidClient.Builder().baseUrl(baseUrl()).build()
        val req = MiataruGetLocationRequest(
            miataruConfig = RequestConfig(requestMiataruDeviceID = "requester-id"),
            miataruGetLocation = listOf(MiataruGetLocationDevice(device = "dev-1"))
        )
        val res = client.getLocation(req)
        assertEquals(1, res.miataruLocation.size)
        assertEquals("dev-1", res.miataruLocation[0].device)
        val recorded = server.takeRequest(2, TimeUnit.SECONDS)
        assertEquals("/GetLocation", recorded?.path)
        assertEquals("POST", recorded?.method)
    }

    @Test
    fun getLocationHistory_success() = runTest {
        val body = """{"MiataruServerConfig":{"MaximumNumberOfLocationUpdates":"100","AvailableDeviceLocationUpdates":"2"},"MiataruLocation":[{"Device":"dev-1","Timestamp":"1441360863","Longitude":"-4.39","Latitude":"41.07","HorizontalAccuracy":"50"}]}"""
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))
        val client = MiataruAndroidClient.Builder().baseUrl(baseUrl()).build()
        val req = MiataruGetLocationHistoryRequest(
            miataruConfig = RequestConfig(requestMiataruDeviceID = "requester-id"),
            miataruGetLocationHistory = GetLocationHistoryPayload(device = "dev-1", amount = "10")
        )
        val res = client.getLocationHistory(req)
        assertEquals(1, res.miataruLocation.size)
        assertEquals("100", res.miataruServerConfig.maximumNumberOfLocationUpdates)
    }

    @Test
    fun getVisitorHistory_success() = runTest {
        val body = """{"MiataruServerConfig":{"MaximumNumberOfVisitorHistory":"50","AvailableVisitorHistory":"1"},"MiataruVisitors":[{"DeviceID":"visitor-1","TimeStamp":"1441360863000"}]}"""
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))
        val client = MiataruAndroidClient.Builder().baseUrl(baseUrl()).build()
        val req = com.miataru.client.model.MiataruGetVisitorHistoryRequest(
            miataruGetVisitorHistory = GetVisitorHistoryPayload(device = "dev-1", amount = "10")
        )
        val res = client.getVisitorHistory(req)
        assertEquals(1, res.miataruVisitors.size)
        assertEquals("visitor-1", res.miataruVisitors[0].deviceId)
    }

    @Test
    fun deleteLocation_success() = runTest {
        server.enqueue(
            MockResponse()
                .setBody("""{"MiataruResponse":"ACK","MiataruVerboseResponse":"Deleted","MiataruDeletedCount":3}""")
                .setResponseCode(200)
        )
        val client = MiataruAndroidClient.Builder().baseUrl(baseUrl()).build()
        val req = MiataruDeleteLocationRequest(
            miataruDeleteLocation = MiataruDeleteLocationPayload(device = "dev-1", deviceKey = "key")
        )
        val res = client.deleteLocation(req)
        assertEquals("ACK", res.miataruResponse)
        assertEquals(3, res.miataruDeletedCount)
        val recorded = server.takeRequest(2, TimeUnit.SECONDS)
        assertEquals("/DeleteLocation", recorded?.path)
        assertEquals("POST", recorded?.method)
    }

    @Test
    fun setDeviceKey_success() = runTest {
        server.enqueue(
            MockResponse()
                .setBody("""{"MiataruResponse":"ACK","MiataruVerboseResponse":"Device key set successfully"}""")
                .setResponseCode(200)
        )
        val client = MiataruAndroidClient.Builder().baseUrl(baseUrl()).build()
        val req = MiataruSetDeviceKeyRequest(
            miataruSetDeviceKey = MiataruSetDeviceKeyPayload(
                deviceId = "dev-1",
                currentDeviceKey = null,
                newDeviceKey = "new-secret"
            )
        )
        val res: MiataruSetDeviceKeyResponse = client.setDeviceKey(req)
        assertEquals("ACK", res.miataruResponse)
        val recorded = server.takeRequest(2, TimeUnit.SECONDS)
        assertEquals("/setDeviceKey", recorded?.path)
        assertEquals("POST", recorded?.method)
    }

    @Test
    fun apiVersion_constant() {
        assertEquals("1.1.0", MiataruAndroidClient.API_VERSION)
    }
}
