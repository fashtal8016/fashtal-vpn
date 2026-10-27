package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VpnState
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProtocolsScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SpeedTestScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: VpnViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val selectedProtocol by viewModel.selectedProtocol.collectAsStateWithLifecycle()
    val durationSeconds by viewModel.durationSeconds.collectAsStateWithLifecycle()
    val downloadSpeedKbps by viewModel.downloadSpeedKbps.collectAsStateWithLifecycle()
    val uploadSpeedKbps by viewModel.uploadSpeedKbps.collectAsStateWithLifecycle()
    val totalDownloadedBytes by viewModel.totalDownloadedBytes.collectAsStateWithLifecycle()
    val totalUploadedBytes by viewModel.totalUploadedBytes.collectAsStateWithLifecycle()

    val filteredServers by viewModel.filteredServers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val countryFilter by viewModel.countryFilter.collectAsStateWithLifecycle()
    val isRefreshingPings by viewModel.isRefreshingPings.collectAsStateWithLifecycle()

    val isKillSwitchActive by viewModel.isKillSwitchActive.collectAsStateWithLifecycle()
    val isDnsProtectionActive by viewModel.isDnsProtectionActive.collectAsStateWithLifecycle()
    val selectedDnsServer by viewModel.selectedDnsServer.collectAsStateWithLifecycle()
    val isBypassLocalTraffic by viewModel.isBypassLocalTraffic.collectAsStateWithLifecycle()
    val autoConnectOnInsecureWifi by viewModel.autoConnectOnInsecureWifi.collectAsStateWithLifecycle()

    val speedTestState by viewModel.speedTestState.collectAsStateWithLifecycle()

    // Handle Back Press when on sub-tabs
    BackHandler(enabled = currentTab != AppTab.HOME) {
        viewModel.selectTab(AppTab.HOME)
    }

    // Android VpnService Permission Launcher
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleConnection(startService = true)
        } else {
            Toast.makeText(context, "VPN Permission required to secure network", Toast.LENGTH_SHORT).show()
        }
    }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handle gracefully
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun handleConnectToggle() {
        if (vpnState == VpnState.CONNECTED || vpnState == VpnState.CONNECTING) {
            viewModel.toggleConnection(startService = false)
        } else {
            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                vpnPrepareLauncher.launch(prepareIntent)
            } else {
                viewModel.toggleConnection(startService = true)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF6F8FC),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF0066FF),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { viewModel.selectTab(AppTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Home",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "VPN",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.HOME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0066FF),
                        selectedTextColor = Color(0xFF0066FF),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SERVERS,
                    onClick = { viewModel.selectTab(AppTab.SERVERS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Servers",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Servers (20)",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SERVERS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0066FF),
                        selectedTextColor = Color(0xFF0066FF),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_servers")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.PROTOCOLS,
                    onClick = { viewModel.selectTab(AppTab.PROTOCOLS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Protocols",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Protocols",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.PROTOCOLS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0066FF),
                        selectedTextColor = Color(0xFF0066FF),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_protocols")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SPEED_TEST,
                    onClick = { viewModel.selectTab(AppTab.SPEED_TEST) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed Test",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Speed",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SPEED_TEST) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0066FF),
                        selectedTextColor = Color(0xFF0066FF),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_speed")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Settings",
                            fontSize = 11.sp,
                            fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0066FF),
                        selectedTextColor = Color(0xFF0066FF),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8),
                        indicatorColor = Color(0xFFEFF6FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        vpnState = vpnState,
                        selectedServer = selectedServer,
                        selectedProtocol = selectedProtocol,
                        durationSeconds = durationSeconds,
                        downloadSpeedKbps = downloadSpeedKbps,
                        uploadSpeedKbps = uploadSpeedKbps,
                        totalDownloadedBytes = totalDownloadedBytes,
                        totalUploadedBytes = totalUploadedBytes,
                        onToggleConnection = { handleConnectToggle() },
                        onOpenServerList = { viewModel.selectTab(AppTab.SERVERS) },
                        onOpenProtocols = { viewModel.selectTab(AppTab.PROTOCOLS) }
                    )
                }
                AppTab.SERVERS -> {
                    ServersScreen(
                        servers = filteredServers,
                        selectedServer = selectedServer,
                        searchQuery = searchQuery,
                        selectedCountryFilter = countryFilter,
                        isRefreshingPings = isRefreshingPings,
                        onSelectServer = { server ->
                            viewModel.selectServer(server)
                            viewModel.selectTab(AppTab.HOME)
                        },
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onCountryFilterChange = { viewModel.setCountryFilter(it) },
                        onRefreshPings = { viewModel.refreshPings() }
                    )
                }
                AppTab.PROTOCOLS -> {
                    ProtocolsScreen(
                        selectedProtocol = selectedProtocol,
                        onSelectProtocol = { protocol ->
                            viewModel.selectProtocol(protocol)
                            viewModel.selectTab(AppTab.HOME)
                        }
                    )
                }
                AppTab.SPEED_TEST -> {
                    SpeedTestScreen(
                        currentServer = selectedServer,
                        speedTestState = speedTestState,
                        onStartTest = { viewModel.runSpeedTest() }
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(
                        isKillSwitchActive = isKillSwitchActive,
                        isDnsProtectionActive = isDnsProtectionActive,
                        selectedDns = selectedDnsServer,
                        isBypassLocalTraffic = isBypassLocalTraffic,
                        autoConnectOnWifi = autoConnectOnInsecureWifi,
                        onToggleKillSwitch = { viewModel.toggleKillSwitch() },
                        onToggleDnsProtection = { viewModel.toggleDnsProtection() },
                        onSelectDns = { viewModel.selectDns(it) },
                        onToggleBypassLocal = { viewModel.toggleBypassLocalTraffic() },
                        onToggleAutoConnectWifi = { viewModel.toggleAutoConnectWifi() }
                    )
                }
            }
        }
    }
}
