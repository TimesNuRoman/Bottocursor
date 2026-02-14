package com.cursorai.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.ConnectionState
import com.cursorai.remote.data.model.DiagnosticSeverity
import com.cursorai.remote.ui.theme.*

/**
 * Status Bar — bottom bar matching Cursor IDE pixel-perfectly.
 * Left: [Remote] branch | errors | warnings
 * Right: Ln,Col | Spaces | Encoding | LF | Language | Notifications
 */
@Composable
fun CursorStatusBar(
    connectionState: ConnectionState,
    gitBranch: String,
    errorCount: Int,
    warningCount: Int,
    line: Int,
    column: Int,
    language: String,
    encoding: String = "UTF-8",
    indentation: String = "Spaces: 2",
    lineEnding: String = "LF",
    latency: Long,
    onBranchClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onEncodingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(StatusBarBg)
    ) {
        // Remote indicator (purple badge)
        if (connectionState == ConnectionState.CONNECTED) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(22.dp)
                    .background(StatusBarRemoteBg)
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Remote",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White
                    )
                }
            }
        } else {
            // Disconnected indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(22.dp)
                    .background(StatusBarBg)
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Rounded.WifiOff,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Offline",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextTertiary
                    )
                }
            }
        }

        // Git branch
        StatusBarItem(
            icon = Icons.Rounded.AccountTree,
            text = gitBranch,
            onClick = onBranchClick
        )

        // Errors
        StatusBarItem(
            icon = Icons.Filled.Cancel,
            iconTint = if (errorCount > 0) StatusError else StatusBarFg,
            text = "$errorCount",
            onClick = {}
        )

        // Warnings
        StatusBarItem(
            icon = Icons.Filled.Warning,
            iconTint = if (warningCount > 0) StatusWarning else StatusBarFg,
            text = "$warningCount",
            onClick = {}
        )

        Spacer(Modifier.weight(1f))

        // Latency (when connected)
        if (connectionState == ConnectionState.CONNECTED && latency > 0) {
            StatusBarItem(
                icon = Icons.Rounded.Speed,
                text = "${latency}ms",
                onClick = {}
            )
        }

        // Line, Column
        StatusBarItem(text = "Ln $line, Col $column", onClick = {})

        // Indentation
        StatusBarItem(text = indentation, onClick = {})

        // Encoding
        StatusBarItem(text = encoding, onClick = onEncodingClick)

        // Line ending
        StatusBarItem(text = lineEnding, onClick = {})

        // Language
        StatusBarItem(text = language.ifBlank { "Plain Text" }, onClick = onLanguageClick)

        // Cursor AI icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(22.dp)
                .padding(horizontal = 8.dp)
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = "Cursor AI",
                tint = CursorPrimary,
                modifier = Modifier.size(13.dp)
            )
        }

        // Bell icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(22.dp)
                .padding(horizontal = 6.dp)
        ) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = StatusBarFg,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
fun StatusBarItem(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color = StatusBarFg,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(22.dp)
            .clip(RoundedCornerShape(2.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(3.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp
            ),
            color = StatusBarFg
        )
    }
}
