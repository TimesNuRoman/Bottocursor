package com.cursorai.remote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.ConnectionState
import com.cursorai.remote.ui.theme.*

@Composable
fun ConnectionStatusBar(
    connectionState: ConnectionState,
    latency: Long,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (connectionState) {
            ConnectionState.CONNECTED -> StatusSuccess
            ConnectionState.CONNECTING -> StatusWarning
            ConnectionState.ERROR -> StatusError
            ConnectionState.DISCONNECTED -> TextTertiary
        },
        animationSpec = tween(300),
        label = "status_color"
    )

    val statusText = when (connectionState) {
        ConnectionState.CONNECTED -> "Connected"
        ConnectionState.CONNECTING -> "Connecting..."
        ConnectionState.ERROR -> "Error"
        ConnectionState.DISCONNECTED -> "Disconnected"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .background(CursorSurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )

            if (connectionState == ConnectionState.CONNECTED && latency > 0) {
                Text(
                    text = "${latency}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        // Connect / Disconnect button
        when (connectionState) {
            ConnectionState.DISCONNECTED, ConnectionState.ERROR -> {
                TextButton(
                    onClick = onConnect,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = CursorPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Rounded.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            ConnectionState.CONNECTED -> {
                TextButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = StatusError
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Rounded.LinkOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Disconnect", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            ConnectionState.CONNECTING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = StatusWarning
                )
            }
        }
    }
}
