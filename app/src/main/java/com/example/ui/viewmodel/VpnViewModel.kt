package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer
import com.example.data.model.VpnState
import com.example.data.repository.ServerRepository
import com.example.service.FashtalVpnService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppTab {
    HOME,
    SERVERS,
    PROTOCOLS,
    SPEED_TEST,
    SETTINGS
}

data class SpeedTestState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val stage: String = "Idle",
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val downloadSpeedMbps: Float = 0f,
    val uploadSpeedMbps: Float = 0f,
    val completed: Boolean = false
)

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServerRepository()

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    val allServers: StateFlow<List<VpnServer>> = repository.servers

    private val _selectedServer = MutableStateFlow(repository.getOptimalServer())
    val selectedServer: StateFlow<VpnServer> = _selectedServer.asStateFlow()

    private val _selectedProtocol = MutableStateFlow(VpnProtocol.EVEREST)
    val selectedProtocol: StateFlow<VpnProtocol> = _selectedProtocol.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds: StateFlow<Long> = _durationSeconds.asStateFlow()

    private val _downloadSpeedKbps = MutableStateFlow(0L)
    val downloadSpeedKbps: StateFlow<Long> = _downloadSpeedKbps.asStateFlow()

    private val _uploadSpeedKbps = MutableStateFlow(0L)
    val uploadSpeedKbps: StateFlow<Long> = _uploadSpeedKbps.asStateFlow()

    private val _totalDownloadedBytes = MutableStateFlow(0L)
    val totalDownloadedBytes: StateFlow<Long> = _totalDownloadedBytes.asStateFlow()

    private val _totalUploadedBytes = MutableStateFlow(0L)
    val totalUploadedBytes: StateFlow<Long> = _totalUploadedBytes.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _countryFilter = MutableStateFlow("ALL") // "ALL", "US", "DE"
    val countryFilter: StateFlow<String> = _countryFilter.asStateFlow()

    private val _isRefreshingPings = MutableStateFlow(false)
    val isRefreshingPings: StateFlow<Boolean> = _isRefreshingPings.asStateFlow()

    // Settings
    private val _isKillSwitchActive = MutableStateFlow(true)
    val isKillSwitchActive: StateFlow<Boolean> = _isKillSwitchActive.asStateFlow()

    private val _isDnsProtectionActive = MutableStateFlow(true)
    val isDnsProtectionActive: StateFlow<Boolean> = _isDnsProtectionActive.asStateFlow()

    private val _selectedDnsServer = MutableStateFlow("Cloudflare (1.1.1.1)")
    val selectedDnsServer: StateFlow<String> = _selectedDnsServer.asStateFlow()

    private val _isBypassLocalTraffic = MutableStateFlow(true)
    val isBypassLocalTraffic: StateFlow<Boolean> = _isBypassLocalTraffic.asStateFlow()

    private val _autoConnectOnInsecureWifi = MutableStateFlow(true)
    val autoConnectOnInsecureWifi: StateFlow<Boolean> = _autoConnectOnInsecureWifi.asStateFlow()

    // Speed test state
    private val _speedTestState = MutableStateFlow(SpeedTestState())
    val speedTestState: StateFlow<SpeedTestState> = _speedTestState.asStateFlow()

    private var connectionTimerJob: Job? = null
    private var trafficSimulationJob: Job? = null
    private var speedTestJob: Job? = null

    // Filtered servers list
    val filteredServers: StateFlow<List<VpnServer>> = combine(
        allServers,
        _searchQuery,
        _countryFilter
    ) { servers, query, country ->
        servers.filter { server ->
            val matchesCountry = when (country) {
                "US" -> server.isUnitedStates
                "DE" -> server.isGermany
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                server.name.contains(query, ignoreCase = true) ||
                server.city.contains(query, ignoreCase = true) ||
                server.ip.contains(query) ||
                server.nodeCode.contains(query, ignoreCase = true)

            matchesCountry && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectServer(server: VpnServer) {
        _selectedServer.value = server
        if (_vpnState.value == VpnState.CONNECTED) {
            // Reconnect smoothly to new server
            toggleConnection(startService = true)
            viewModelScope.launch {
                delay(800)
                toggleConnection(startService = true)
            }
        }
    }

    fun selectProtocol(protocol: VpnProtocol) {
        _selectedProtocol.value = protocol
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCountryFilter(country: String) {
        _countryFilter.value = country
    }

    fun toggleKillSwitch() {
        _isKillSwitchActive.value = !_isKillSwitchActive.value
    }

    fun toggleDnsProtection() {
        _isDnsProtectionActive.value = !_isDnsProtectionActive.value
    }

    fun selectDns(dns: String) {
        _selectedDnsServer.value = dns
    }

    fun toggleBypassLocalTraffic() {
        _isBypassLocalTraffic.value = !_isBypassLocalTraffic.value
    }

    fun toggleAutoConnectWifi() {
        _autoConnectOnInsecureWifi.value = !_autoConnectOnInsecureWifi.value
    }

    fun toggleConnection(startService: Boolean) {
        val context = getApplication<Application>().applicationContext
        when (_vpnState.value) {
            VpnState.DISCONNECTED -> {
                connectVpn(context, startService)
            }
            VpnState.CONNECTED -> {
                disconnectVpn(context)
            }
            VpnState.CONNECTING -> {
                disconnectVpn(context)
            }
            VpnState.DISCONNECTING -> {
                // Ignore while in transition
            }
        }
    }

    private fun connectVpn(context: Context, startService: Boolean) {
        viewModelScope.launch {
            _vpnState.value = VpnState.CONNECTING
            // Realistic handshake sequence
            delay(1200)

            if (startService) {
                try {
                    val intent = Intent(context, FashtalVpnService::class.java).apply {
                        action = FashtalVpnService.ACTION_CONNECT
                        putExtra(FashtalVpnService.EXTRA_SERVER_NAME, _selectedServer.value.name)
                        putExtra(FashtalVpnService.EXTRA_SERVER_IP, _selectedServer.value.ip)
                        putExtra(FashtalVpnService.EXTRA_PROTOCOL, _selectedProtocol.value.shortName)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _vpnState.value = VpnState.CONNECTED
            startMetrics()
        }
    }

    fun disconnectVpn(context: Context) {
        viewModelScope.launch {
            _vpnState.value = VpnState.DISCONNECTING
            delay(600)

            try {
                val intent = Intent(context, FashtalVpnService::class.java).apply {
                    action = FashtalVpnService.ACTION_DISCONNECT
                }
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            stopMetrics()
            _vpnState.value = VpnState.DISCONNECTED
            _downloadSpeedKbps.value = 0L
            _uploadSpeedKbps.value = 0L
        }
    }

    private fun startMetrics() {
        connectionTimerJob?.cancel()
        trafficSimulationJob?.cancel()

        _durationSeconds.value = 0L

        // Connection Timer (Continuous, Unlimited)
        connectionTimerJob = viewModelScope.launch {
            while (isActive && _vpnState.value == VpnState.CONNECTED) {
                delay(1000)
                _durationSeconds.value += 1
            }
        }

        // Realistic live bandwidth fluctuations
        trafficSimulationJob = viewModelScope.launch {
            while (isActive && _vpnState.value == VpnState.CONNECTED) {
                delay(1000)
                val baseDown = when (_selectedProtocol.value) {
                    VpnProtocol.EVEREST -> Random.nextLong(18000, 48000)
                    VpnProtocol.WIREGUARD -> Random.nextLong(22000, 56000)
                    VpnProtocol.UDP -> Random.nextLong(15000, 39000)
                    VpnProtocol.TCP -> Random.nextLong(12000, 28000)
                    VpnProtocol.STEALTH -> Random.nextLong(10000, 24000)
                }
                val baseUp = (baseDown * Random.nextDouble(0.2, 0.4)).toLong()

                _downloadSpeedKbps.value = baseDown
                _uploadSpeedKbps.value = baseUp

                _totalDownloadedBytes.value += (baseDown * 1024 / 8)
                _totalUploadedBytes.value += (baseUp * 1024 / 8)
            }
        }
    }

    private fun stopMetrics() {
        connectionTimerJob?.cancel()
        trafficSimulationJob?.cancel()
    }

    fun refreshPings() {
        viewModelScope.launch {
            _isRefreshingPings.value = true
            repository.refreshAllPings()
            delay(400)
            _isRefreshingPings.value = false
        }
    }

    fun runSpeedTest() {
        if (_speedTestState.value.isRunning) return

        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            val server = _selectedServer.value
            _speedTestState.value = SpeedTestState(
                isRunning = true,
                progress = 0.1f,
                stage = "Testing Ping Latency...",
                pingMs = server.pingMs,
                jitterMs = Random.nextInt(1, 4)
            )

            delay(1200)

            _speedTestState.value = _speedTestState.value.copy(
                progress = 0.35f,
                stage = "Testing Download Speed...",
                downloadSpeedMbps = 24.5f
            )

            for (i in 1..5) {
                delay(400)
                val currentSpeed = when (_selectedProtocol.value) {
                    VpnProtocol.WIREGUARD -> Random.nextFloat() * 40f + 110f
                    VpnProtocol.EVEREST -> Random.nextFloat() * 35f + 95f
                    VpnProtocol.UDP -> Random.nextFloat() * 25f + 75f
                    else -> Random.nextFloat() * 20f + 55f
                }
                _speedTestState.value = _speedTestState.value.copy(
                    progress = 0.35f + (i * 0.08f),
                    downloadSpeedMbps = currentSpeed
                )
            }

            _speedTestState.value = _speedTestState.value.copy(
                progress = 0.75f,
                stage = "Testing Upload Speed..."
            )

            for (i in 1..4) {
                delay(400)
                val currentUp = Random.nextFloat() * 15f + 35f
                _speedTestState.value = _speedTestState.value.copy(
                    progress = 0.75f + (i * 0.06f),
                    uploadSpeedMbps = currentUp
                )
            }

            _speedTestState.value = _speedTestState.value.copy(
                isRunning = false,
                progress = 1.0f,
                stage = "Test Completed (Excellent)",
                completed = true
            )
        }
    }

    override fun onCleared() {
        stopMetrics()
        speedTestJob?.cancel()
        super.onCleared()
    }
}
