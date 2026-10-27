package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FashtalVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var trafficJob: Job? = null

    companion object {
        const val ACTION_CONNECT = "com.example.fashtalvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.fashtalvpn.DISCONNECT"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_SERVER_IP = "extra_server_ip"
        const val EXTRA_PROTOCOL = "extra_protocol"

        const val CHANNEL_ID = "fashtal_vpn_channel"
        const val NOTIFICATION_ID = 1001

        var isServiceRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_CONNECT -> {
                val serverName = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "Optimal Node"
                val serverIp = intent.getStringExtra(EXTRA_SERVER_IP) ?: "104.28.19.42"
                val protocol = intent.getStringExtra(EXTRA_PROTOCOL) ?: "Protocol E"
                startVpnTunnel(serverName, serverIp, protocol)
            }
            ACTION_DISCONNECT -> {
                stopVpnTunnel()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startVpnTunnel(serverName: String, serverIp: String, protocol: String) {
        try {
            val builder = Builder()
                .setSession("Fashtal VPN ($serverName)")
                .addAddress("10.8.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1")
                .addDnsServer("8.8.8.8")
                .setMtu(1500)

            vpnInterface = builder.establish()
            isServiceRunning = true

            val notification = buildNotification("Connected to $serverName ($serverIp)", protocol)
            startForeground(NOTIFICATION_ID, notification)

            startTrafficSimulation()
        } catch (e: Exception) {
            e.printStackTrace()
            stopVpnTunnel()
        }
    }

    private fun startTrafficSimulation() {
        trafficJob?.cancel()
        trafficJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                // Maintain active tunnel lifecycle
            }
        }
    }

    private fun stopVpnTunnel() {
        trafficJob?.cancel()
        isServiceRunning = false
        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopVpnTunnel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopVpnTunnel()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fashtal VPN Tunnel Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time status of Fashtal VPN connection"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String, protocol: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val disconnectIntent = Intent(this, FashtalVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fashtal VPN • Connected")
            .setContentText("$statusText • $protocol")
            .setSmallIcon(R.drawable.fashtal_logo)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect",
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
