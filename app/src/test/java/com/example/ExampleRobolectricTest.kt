package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ServerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Fashtal VPN", appName)
    }

    @Test
    fun `verify exactly 20 clean servers from US and Germany`() {
        val repository = ServerRepository()
        val servers = repository.servers.value
        assertEquals(20, servers.size)

        val usServers = servers.filter { it.isUnitedStates }
        val deServers = servers.filter { it.isGermany }

        assertEquals(10, usServers.size)
        assertEquals(10, deServers.size)

        // All servers must have low ping and non-empty clean IP
        servers.forEach { server ->
            assertTrue(server.ip.isNotEmpty())
            assertTrue(server.pingMs > 0 && server.pingMs < 100)
        }
    }
}
