package com.example.data.model

enum class VpnProtocol(
    val code: String,
    val title: String,
    val shortName: String,
    val description: String,
    val badge: String,
    val portRecommendation: String,
    val isRecommended: Boolean = false
) {
    EVEREST(
        code = "E",
        title = "Protocol E (Everest)",
        shortName = "Everest",
        description = "Proprietary high-speed obfuscation. Effortlessly bypasses strict network filters and throttling.",
        badge = "Optimal",
        portRecommendation = "Port 443 (TLS)",
        isRecommended = true
    ),
    WIREGUARD(
        code = "V",
        title = "Protocol V (WireGuard)",
        shortName = "WireGuard",
        description = "Cutting-edge modern cryptography with lowest CPU usage and maximum transfer speed.",
        badge = "Ultra Fast",
        portRecommendation = "Port 51820 (UDP)"
    ),
    UDP(
        code = "U",
        title = "Protocol U (UDP Turbo)",
        shortName = "UDP Turbo",
        description = "Optimized for lowest latency. Best choice for competitive online gaming and buffer-free HD streaming.",
        badge = "Low Latency",
        portRecommendation = "Port 8443 (UDP)"
    ),
    TCP(
        code = "T",
        title = "Protocol T (TCP Stable)",
        shortName = "TCP Stable",
        description = "Rock-solid connection stability with packet acknowledgment. Works reliably even on unstable Wi-Fi.",
        badge = "Stable",
        portRecommendation = "Port 8080 (TCP)"
    ),
    STEALTH(
        code = "S",
        title = "Protocol S (Stealth / TLS)",
        shortName = "Stealth",
        description = "Masks VPN traffic as regular HTTPS web browsing. Completely evades deep packet inspection (DPI).",
        badge = "Anti-Filter",
        portRecommendation = "Port 443 (TLS Camouflage)"
    )
}
