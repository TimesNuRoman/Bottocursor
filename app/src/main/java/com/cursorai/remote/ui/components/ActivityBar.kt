package com.cursorai.remote.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cursorai.remote.data.model.SidebarPanel
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.theme.*

/**
 * Activity Bar — thin icon strip on the far left, identical to Cursor IDE.
 * 48dp wide, dark background, icons with active indicator.
 */
@Composable
fun ActivityBar(
    activePanel: SidebarPanel,
    sidebarVisible: Boolean,
    voiceState: VoiceState,
    onPanelSelect: (SidebarPanel) -> Unit,
    onVoiceToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .background(ActivityBarBg)
    ) {
        // Top section icons
        ActivityBarIcon(
            icon = Icons.Outlined.Description,
            label = "Explorer",
            isActive = activePanel == SidebarPanel.EXPLORER && sidebarVisible,
            onClick = { onPanelSelect(SidebarPanel.EXPLORER) }
        )
        ActivityBarIcon(
            icon = Icons.Outlined.Search,
            label = "Search",
            isActive = activePanel == SidebarPanel.SEARCH && sidebarVisible,
            onClick = { onPanelSelect(SidebarPanel.SEARCH) }
        )
        ActivityBarIcon(
            icon = Icons.Outlined.AccountTree,
            label = "Source Control",
            isActive = activePanel == SidebarPanel.GIT && sidebarVisible,
            onClick = { onPanelSelect(SidebarPanel.GIT) }
        )
        ActivityBarIcon(
            icon = Icons.Outlined.BugReport,
            label = "Run & Debug",
            isActive = activePanel == SidebarPanel.DEBUG && sidebarVisible,
            onClick = { onPanelSelect(SidebarPanel.DEBUG) }
        )
        ActivityBarIcon(
            icon = Icons.Outlined.Extension,
            label = "Extensions",
            isActive = activePanel == SidebarPanel.EXTENSIONS && sidebarVisible,
            onClick = { onPanelSelect(SidebarPanel.EXTENSIONS) }
        )

        Spacer(Modifier.height(4.dp))

        // Divider
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(1.dp)
                .background(PanelBorder)
        )

        Spacer(Modifier.height(4.dp))

        // AI Chat
        ActivityBarIcon(
            icon = Icons.Outlined.AutoAwesome,
            label = "Cursor AI",
            isActive = activePanel == SidebarPanel.AI_CHAT && sidebarVisible,
            onClick = { onPanelSelect(SidebarPanel.AI_CHAT) },
            tintActive = CursorPrimary
        )

        // Project Plan
        ActivityBarIcon(
            icon = Icons.Outlined.RocketLaunch,
            label = "Project Plan",
            isActive = activePanel == SidebarPanel.PROJECT_PLAN && sidebarVisible,
            onClick = { onPanelSelect(SidebarPanel.PROJECT_PLAN) },
            tintActive = CursorTertiary
        )

        Spacer(Modifier.weight(1f))

        // Voice button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onVoiceToggle
                )
        ) {
            val micColor = when (voiceState) {
                VoiceState.LISTENING -> CursorPrimary
                VoiceState.PROCESSING -> StatusWarning
                else -> ActivityBarFg
            }
            Icon(
                imageVector = if (voiceState == VoiceState.LISTENING) Icons.Rounded.MicNone
                else Icons.Outlined.Mic,
                contentDescription = "Voice Input",
                tint = micColor,
                modifier = Modifier.size(22.dp)
            )
        }

        // Settings icon
        ActivityBarIcon(
            icon = Icons.Outlined.Settings,
            label = "Settings",
            isActive = false,
            onClick = onSettingsClick
        )

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun ActivityBarIcon(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tintActive: Color = ActivityBarActive
) {
    val iconColor by animateColorAsState(
        targetValue = if (isActive) tintActive else ActivityBarFg,
        animationSpec = tween(150),
        label = "icon_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Active indicator (left strip)
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .background(ActivityBarIndicator)
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
