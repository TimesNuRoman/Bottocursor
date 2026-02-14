package com.cursorai.remote.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cursorai.remote.data.model.ConnectionState
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.AppTab
import com.cursorai.remote.ui.MainViewModel
import com.cursorai.remote.ui.components.ConnectionStatusBar
import com.cursorai.remote.ui.components.VoiceButton
import com.cursorai.remote.ui.components.VoiceOverlay
import com.cursorai.remote.ui.theme.*

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    widthSizeClass: WindowWidthSizeClass
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val latency by viewModel.latency.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val partialText by viewModel.partialText.collectAsState()
    val showVoiceOverlay by viewModel.showVoiceOverlay.collectAsState()
    val commandHistory by viewModel.commandHistory.collectAsState()

    // Editor state
    val editorState by viewModel.editorState.collectAsState()
    val fileTree by viewModel.fileTree.collectAsState()
    val terminalLines by viewModel.terminalLines.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Settings
    val connectionConfig by viewModel.connectionConfig.collectAsState()
    val voiceLanguage by viewModel.voiceLanguage.collectAsState()
    val autoConnect by viewModel.autoConnect.collectAsState()
    val hapticFeedback by viewModel.hapticFeedback.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()

    val isTablet = widthSizeClass != WindowWidthSizeClass.Compact

    Box(modifier = Modifier.fillMaxSize()) {
        if (isTablet) {
            // Tablet layout: Rail navigation + split panels
            TabletLayout(
                currentTab = currentTab,
                connectionState = connectionState,
                latency = latency,
                voiceState = voiceState,
                amplitude = amplitude,
                editorState = editorState,
                fileTree = fileTree,
                terminalLines = terminalLines,
                chatMessages = chatMessages,
                connectionConfig = connectionConfig,
                voiceLanguage = voiceLanguage,
                autoConnect = autoConnect,
                hapticFeedback = hapticFeedback,
                fontSize = fontSize,
                viewModel = viewModel,
                onTabSelect = { viewModel.setCurrentTab(it) },
                onVoiceToggle = { viewModel.toggleVoiceOverlay() }
            )
        } else {
            // Phone layout: Bottom navigation
            PhoneLayout(
                currentTab = currentTab,
                connectionState = connectionState,
                latency = latency,
                voiceState = voiceState,
                amplitude = amplitude,
                editorState = editorState,
                fileTree = fileTree,
                terminalLines = terminalLines,
                chatMessages = chatMessages,
                connectionConfig = connectionConfig,
                voiceLanguage = voiceLanguage,
                autoConnect = autoConnect,
                hapticFeedback = hapticFeedback,
                fontSize = fontSize,
                viewModel = viewModel,
                onTabSelect = { viewModel.setCurrentTab(it) },
                onVoiceToggle = { viewModel.toggleVoiceOverlay() }
            )
        }

        // Voice overlay
        VoiceOverlay(
            visible = showVoiceOverlay,
            voiceState = voiceState,
            amplitude = amplitude,
            partialText = partialText,
            commandHistory = commandHistory,
            quickActions = viewModel.quickActions,
            onStartListening = { viewModel.startVoiceInput() },
            onStopListening = { viewModel.stopVoiceInput() },
            onQuickAction = { viewModel.executeQuickAction(it) },
            onDismiss = { viewModel.toggleVoiceOverlay() }
        )
    }
}

@Composable
fun PhoneLayout(
    currentTab: AppTab,
    connectionState: ConnectionState,
    latency: Long,
    voiceState: VoiceState,
    amplitude: Float,
    editorState: com.cursorai.remote.data.model.EditorState,
    fileTree: List<com.cursorai.remote.data.model.FileNode>,
    terminalLines: List<com.cursorai.remote.data.model.TerminalLine>,
    chatMessages: List<com.cursorai.remote.data.model.ChatMessage>,
    connectionConfig: com.cursorai.remote.data.model.ConnectionConfig,
    voiceLanguage: String,
    autoConnect: Boolean,
    hapticFeedback: Boolean,
    fontSize: Int,
    viewModel: MainViewModel,
    onTabSelect: (AppTab) -> Unit,
    onVoiceToggle: () -> Unit
) {
    Scaffold(
        containerColor = CursorSurface,
        bottomBar = {
            BottomNavigationBar(
                currentTab = currentTab,
                voiceState = voiceState,
                amplitude = amplitude,
                onTabSelect = onTabSelect,
                onVoiceToggle = onVoiceToggle
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Connection status
            ConnectionStatusBar(
                connectionState = connectionState,
                latency = latency,
                onConnect = { viewModel.connect() },
                onDisconnect = { viewModel.disconnect() }
            )

            // Content
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(200))
                },
                modifier = Modifier.weight(1f),
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    AppTab.EDITOR -> EditorScreen(
                        editorState = editorState,
                        onAction = { viewModel.webSocketManager.sendEditorAction(it) }
                    )
                    AppTab.FILES -> FileExplorerScreen(
                        fileTree = fileTree,
                        onFileSelect = { viewModel.requestFile(it) },
                        onRefresh = { viewModel.webSocketManager.requestFileTree() }
                    )
                    AppTab.TERMINAL -> TerminalScreen(
                        terminalLines = terminalLines,
                        voiceState = voiceState,
                        onSendCommand = { viewModel.sendTerminalCommand(it) },
                        onVoiceInput = onVoiceToggle
                    )
                    AppTab.AI_CHAT -> AIChatScreen(
                        messages = chatMessages,
                        voiceState = voiceState,
                        onSendMessage = { viewModel.sendAIMessage(it) },
                        onVoiceInput = onVoiceToggle
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        connectionConfig = connectionConfig,
                        connectionState = connectionState,
                        voiceLanguage = voiceLanguage,
                        autoConnect = autoConnect,
                        hapticFeedback = hapticFeedback,
                        fontSize = fontSize,
                        onSaveConfig = { viewModel.saveSettings(it) },
                        onConnect = { viewModel.connect() },
                        onDisconnect = { viewModel.disconnect() },
                        onSaveVoiceLanguage = { viewModel.saveVoiceLanguage(it) },
                        onSaveAutoConnect = { viewModel.saveAutoConnect(it) },
                        onSaveHapticFeedback = { viewModel.saveHapticFeedback(it) },
                        onSaveFontSize = { viewModel.saveFontSize(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun TabletLayout(
    currentTab: AppTab,
    connectionState: ConnectionState,
    latency: Long,
    voiceState: VoiceState,
    amplitude: Float,
    editorState: com.cursorai.remote.data.model.EditorState,
    fileTree: List<com.cursorai.remote.data.model.FileNode>,
    terminalLines: List<com.cursorai.remote.data.model.TerminalLine>,
    chatMessages: List<com.cursorai.remote.data.model.ChatMessage>,
    connectionConfig: com.cursorai.remote.data.model.ConnectionConfig,
    voiceLanguage: String,
    autoConnect: Boolean,
    hapticFeedback: Boolean,
    fontSize: Int,
    viewModel: MainViewModel,
    onTabSelect: (AppTab) -> Unit,
    onVoiceToggle: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail
        NavigationRailBar(
            currentTab = currentTab,
            voiceState = voiceState,
            amplitude = amplitude,
            onTabSelect = onTabSelect,
            onVoiceToggle = onVoiceToggle
        )

        // Main content area
        Column(modifier = Modifier.weight(1f)) {
            // Connection status
            ConnectionStatusBar(
                connectionState = connectionState,
                latency = latency,
                onConnect = { viewModel.connect() },
                onDisconnect = { viewModel.disconnect() }
            )

            when (currentTab) {
                AppTab.EDITOR -> {
                    // Tablet: File explorer + Editor side by side
                    Row(modifier = Modifier.weight(1f)) {
                        FileExplorerScreen(
                            fileTree = fileTree,
                            onFileSelect = { viewModel.requestFile(it) },
                            onRefresh = { viewModel.webSocketManager.requestFileTree() },
                            modifier = Modifier.width(260.dp)
                        )

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(BorderDefault)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            EditorScreen(
                                editorState = editorState,
                                onAction = { viewModel.webSocketManager.sendEditorAction(it) },
                                modifier = Modifier.weight(0.65f)
                            )

                            // Divider
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BorderDefault)
                            )

                            // Terminal below editor
                            TerminalScreen(
                                terminalLines = terminalLines,
                                voiceState = voiceState,
                                onSendCommand = { viewModel.sendTerminalCommand(it) },
                                onVoiceInput = onVoiceToggle,
                                modifier = Modifier.weight(0.35f)
                            )
                        }
                    }
                }

                AppTab.FILES -> FileExplorerScreen(
                    fileTree = fileTree,
                    onFileSelect = { viewModel.requestFile(it) },
                    onRefresh = { viewModel.webSocketManager.requestFileTree() }
                )

                AppTab.TERMINAL -> TerminalScreen(
                    terminalLines = terminalLines,
                    voiceState = voiceState,
                    onSendCommand = { viewModel.sendTerminalCommand(it) },
                    onVoiceInput = onVoiceToggle
                )

                AppTab.AI_CHAT -> {
                    // Tablet: Chat + Editor side by side
                    Row(modifier = Modifier.weight(1f)) {
                        AIChatScreen(
                            messages = chatMessages,
                            voiceState = voiceState,
                            onSendMessage = { viewModel.sendAIMessage(it) },
                            onVoiceInput = onVoiceToggle,
                            modifier = Modifier.weight(0.45f)
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(BorderDefault)
                        )

                        EditorScreen(
                            editorState = editorState,
                            onAction = { viewModel.webSocketManager.sendEditorAction(it) },
                            modifier = Modifier.weight(0.55f)
                        )
                    }
                }

                AppTab.SETTINGS -> SettingsScreen(
                    connectionConfig = connectionConfig,
                    connectionState = connectionState,
                    voiceLanguage = voiceLanguage,
                    autoConnect = autoConnect,
                    hapticFeedback = hapticFeedback,
                    fontSize = fontSize,
                    onSaveConfig = { viewModel.saveSettings(it) },
                    onConnect = { viewModel.connect() },
                    onDisconnect = { viewModel.disconnect() },
                    onSaveVoiceLanguage = { viewModel.saveVoiceLanguage(it) },
                    onSaveAutoConnect = { viewModel.saveAutoConnect(it) },
                    onSaveHapticFeedback = { viewModel.saveHapticFeedback(it) },
                    onSaveFontSize = { viewModel.saveFontSize(it) }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: AppTab,
    voiceState: VoiceState,
    amplitude: Float,
    onTabSelect: (AppTab) -> Unit,
    onVoiceToggle: () -> Unit
) {
    val tabs = listOf(
        Triple(AppTab.EDITOR, Icons.Rounded.Code, "Editor"),
        Triple(AppTab.FILES, Icons.Rounded.FolderOpen, "Files"),
        Triple(AppTab.AI_CHAT, Icons.Rounded.AutoAwesome, "AI Chat"),
        Triple(AppTab.TERMINAL, Icons.Rounded.Terminal, "Terminal"),
        Triple(AppTab.SETTINGS, Icons.Rounded.Settings, "Settings")
    )

    Surface(
        color = CursorSurfaceVariant,
        shadowElevation = 8.dp
    ) {
        Column {
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderDefault)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                tabs.forEachIndexed { index, (tab, icon, label) ->
                    if (index == 2) {
                        // Voice button in the center
                        VoiceButton(
                            voiceState = voiceState,
                            amplitude = amplitude,
                            onClick = onVoiceToggle,
                            size = 48.dp
                        )
                    }

                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { onTabSelect(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (currentTab == tab) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CursorPrimary,
                            selectedTextColor = CursorPrimary,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary,
                            indicatorColor = CursorPrimary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationRailBar(
    currentTab: AppTab,
    voiceState: VoiceState,
    amplitude: Float,
    onTabSelect: (AppTab) -> Unit,
    onVoiceToggle: () -> Unit
) {
    val tabs = listOf(
        Triple(AppTab.EDITOR, Icons.Rounded.Code, "Editor"),
        Triple(AppTab.FILES, Icons.Rounded.FolderOpen, "Files"),
        Triple(AppTab.AI_CHAT, Icons.Rounded.AutoAwesome, "AI"),
        Triple(AppTab.TERMINAL, Icons.Rounded.Terminal, "Terminal"),
        Triple(AppTab.SETTINGS, Icons.Rounded.Settings, "Settings")
    )

    NavigationRail(
        containerColor = CursorSurfaceVariant,
        contentColor = TextPrimary,
        header = {
            Spacer(Modifier.height(12.dp))
            // Voice button at top of rail
            VoiceButton(
                voiceState = voiceState,
                amplitude = amplitude,
                onClick = onVoiceToggle,
                size = 44.dp
            )
            Spacer(Modifier.height(8.dp))
        }
    ) {
        tabs.forEach { (tab, icon, label) ->
            NavigationRailItem(
                selected = currentTab == tab,
                onClick = { onTabSelect(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (currentTab == tab) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = CursorPrimary,
                    selectedTextColor = CursorPrimary,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary,
                    indicatorColor = CursorPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}
