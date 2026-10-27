package com.example.data.model

data class VpnServer(
    val id: String,
    val name: String,
    val country: String, // "United States" or "Germany"
    val countryCode: String, // "US" or "DE"
    val city: String,
    val ip: String,
    val port: Int,
    var pingMs: Int,
    val loadPercent: Int,
    val nodeCode: String,
    val supportedProtocols: List<VpnProtocol> = listOf(
        VpnProtocol.EVEREST,
        VpnProtocol.WIREGUARD,
        VpnProtocol.TCP,
        VpnProtocol.UDP
    ),
    val isOptimal: Boolean = false
) {
    val isUnitedStates: Boolean get() = countryCode.equals("US", ignoreCase = true)
    val isGermany: Boolean get() = countryCode.equals("DE", ignoreCase = true)
}
