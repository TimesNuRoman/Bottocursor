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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.ConnectionState
import com.cursorai.remote.ui.theme.*

/**
 * Title Bar — top bar matching Cursor IDE exactly.
 * Shows: window controls (macOS style) | Navigation | Project name | Search bar
 */
@Composable
fun CursorTitleBar(
    projectName: String,
    activeFileName: String,
    connectionState: ConnectionState,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onSearch: () -> Unit,
    onToggleSidebar: () -> Unit,
    onTogglePanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connectionColor by animateColorAsState(
        targetValue = when (connectionState) {
            ConnectionState.CONNECTED -> StatusSuccess
            ConnectionState.CONNECTING -> StatusWarning
            ConnectionState.ERROR -> StatusError
            ConnectionState.DISCONNECTED -> TextTertiary
        },
        animationSpec = tween(300),
        label = "conn"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(TitleBarBg)
            .padding(horizontal = 8.dp)
    ) {
        // macOS-style traffic lights (decorative)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
        ) {
            TrafficLight(color = StatusError)
            TrafficLight(color = StatusWarning)
            TrafficLight(color = StatusSuccess)
        }

        // Navigation arrows
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TitleBarFg,
                modifier = Modifier.size(14.dp)
            )
        }
        IconButton(
            onClick = onForward,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = "Forward",
                tint = TitleBarFg,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        // Toggle sidebar
        IconButton(
            onClick = onToggleSidebar,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Rounded.ViewSidebar,
                contentDescription = "Toggle sidebar",
                tint = TitleBarFg,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // Center: project name + file
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Connection indicator dot
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(connectionColor)
            )
            Spacer(Modifier.width(6.dp))

            Text(
                text = if (activeFileName.isNotBlank()) "$activeFileName - " else "",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = TitleBarFg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = projectName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = TitleBarFg.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = " - Cursor",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = TitleBarFg.copy(alpha = 0.5f),
            )
        }

        Spacer(Modifier.weight(1f))

        // Search bar (collapsed)
        Surface(
            onClick = onSearch,
            shape = RoundedCornerShape(6.dp),
            color = StatusBarItemHover,
            modifier = Modifier.height(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = TitleBarFg.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TitleBarFg.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Toggle panel
        IconButton(
            onClick = onTogglePanel,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Rounded.WebAsset,
                contentDescription = "Toggle panel",
                tint = TitleBarFg,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun TrafficLight(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.8f))
    )
}
