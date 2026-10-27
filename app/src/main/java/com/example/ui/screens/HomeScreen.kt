package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.VpnProtocol
import com.example.data.model.VpnServer
import com.example.data.model.VpnState
import com.example.ui.components.ConnectButton
import com.example.ui.components.ServerCard
import com.example.ui.components.StatsPanel
import com.example.ui.components.VipAdFreeBanner

@Composable
fun HomeScreen(
    vpnState: VpnState,
    selectedServer: VpnServer,
    selectedProtocol: VpnProtocol,
    durationSeconds: Long,
    downloadSpeedKbps: Long,
    uploadSpeedKbps: Long,
    totalDownloadedBytes: Long,
    totalUploadedBytes: Long,
    onToggleConnection: () -> Unit,
    onOpenServerList: () -> Unit,
    onOpenProtocols: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FC))
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.fashtal_logo),
                    contentDescription = "Fashtal VPN Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Fashtal VPN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Ultra-Fast & Stable",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Clean IP Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (vpnState == VpnState.CONNECTED) Color(0xFF10B981) else Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (vpnState == VpnState.CONNECTED) "Protected" else "Unprotected",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (vpnState == VpnState.CONNECTED) Color(0xFF059669) else Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VIP / Ad-Free / Unlimited Banner
        VipAdFreeBanner()

        Spacer(modifier = Modifier.height(24.dp))

        // Center Connect Button (X-VPN Style)
        ConnectButton(
            vpnState = vpnState,
            onToggle = onToggleConnection
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Current Server Selector Card
        ServerCard(
            server = selectedServer,
            protocol = selectedProtocol,
            onClick = onOpenServerList
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Live Real-Time Stats (Speeds & Unlimited duration)
        StatsPanel(
            vpnState = vpnState,
            durationSeconds = durationSeconds,
            downloadSpeedKbps = downloadSpeedKbps,
            uploadSpeedKbps = uploadSpeedKbps,
            totalDownloadedBytes = totalDownloadedBytes,
            totalUploadedBytes = totalUploadedBytes
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Security feature badges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecurityFeatureChip(
                icon = Icons.Default.Lock,
                title = "Kill Switch",
                status = "Active",
                modifier = Modifier.weight(1f)
            )
            SecurityFeatureChip(
                icon = Icons.Default.Security,
                title = "DNS Shield",
                status = "Zero-Leak",
                modifier = Modifier.weight(1f)
            )
            SecurityFeatureChip(
                icon = Icons.Default.Speed,
                title = "Clean Routing",
                status = "Fast-Lane",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SecurityFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0066FF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = status,
                fontSize = 10.sp,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
