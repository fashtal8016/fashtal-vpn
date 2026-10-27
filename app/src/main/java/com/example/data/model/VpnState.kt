package com.example.data.model

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING;

    val isConnected: Boolean get() = this == CONNECTED
    val isBusy: Boolean get() = this == CONNECTING || this == DISCONNECTING
}
