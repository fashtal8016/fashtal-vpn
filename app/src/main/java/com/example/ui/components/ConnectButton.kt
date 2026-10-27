package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VpnState

@Composable
fun ConnectButton(
    vpnState: VpnState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")

    // Pulse animation when connecting or connected
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (vpnState == VpnState.CONNECTING || vpnState == VpnState.CONNECTED) 1.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale_1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = if (vpnState == VpnState.CONNECTING || vpnState == VpnState.CONNECTED) 0.35f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (vpnState == VpnState.CONNECTING || vpnState == VpnState.CONNECTED) 1.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale_2"
    )

    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = if (vpnState == VpnState.CONNECTING || vpnState == VpnState.CONNECTED) 0.25f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha_2"
    )

    val buttonColor1 by animateColorAsState(
        targetValue = when (vpnState) {
            VpnState.DISCONNECTED -> Color(0xFF0066FF)
            VpnState.CONNECTING -> Color(0xFF0284C7)
            VpnState.CONNECTED -> Color(0xFF10B981)
            VpnState.DISCONNECTING -> Color(0xFF64748B)
        },
        animationSpec = tween(500),
        label = "button_color_1"
    )

    val buttonColor2 by animateColorAsState(
        targetValue = when (vpnState) {
            VpnState.DISCONNECTED -> Color(0xFF0052CC)
            VpnState.CONNECTING -> Color(0xFF0369A1)
            VpnState.CONNECTED -> Color(0xFF059669)
            VpnState.DISCONNECTING -> Color(0xFF475569)
        },
        animationSpec = tween(500),
        label = "button_color_2"
    )

    val outerRingColor by animateColorAsState(
        targetValue = when (vpnState) {
            VpnState.DISCONNECTED -> Color(0x220066FF)
            VpnState.CONNECTING -> Color(0x330284C7)
            VpnState.CONNECTED -> Color(0x3310B981)
            VpnState.DISCONNECTING -> Color(0x2264748B)
        },
        animationSpec = tween(500),
        label = "outer_ring_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Outer pulse waves
            if (vpnState == VpnState.CONNECTING || vpnState == VpnState.CONNECTED) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale2)
                        .clip(CircleShape)
                        .background(buttonColor1.copy(alpha = pulseAlpha2))
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale1)
                        .clip(CircleShape)
                        .background(buttonColor1.copy(alpha = pulseAlpha1))
                )
            }

            // Static soft outer halo
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(outerRingColor)
            )

            // Inner border ring
            Box(
                modifier = Modifier
                    .size(168.dp)
                    .clip(CircleShape)
                    .border(3.dp, buttonColor1.copy(alpha = 0.4f), CircleShape)
            )

            // Primary Connect Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(144.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(buttonColor1, buttonColor2)
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = Color.White),
                        onClick = onToggle
                    )
                    .testTag("vpn_connect_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (vpnState == VpnState.CONNECTED) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                        contentDescription = "VPN Power Toggle",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State indicator text
        val statusText = when (vpnState) {
            VpnState.DISCONNECTED -> "TAP TO CONNECT"
            VpnState.CONNECTING -> "CONNECTING..."
            VpnState.CONNECTED -> "CONNECTED • SECURE"
            VpnState.DISCONNECTING -> "DISCONNECTING..."
        }

        val statusColor = when (vpnState) {
            VpnState.DISCONNECTED -> Color(0xFF64748B)
            VpnState.CONNECTING -> Color(0xFF0284C7)
            VpnState.CONNECTED -> Color(0xFF10B981)
            VpnState.DISCONNECTING -> Color(0xFF64748B)
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    }
}
