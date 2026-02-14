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
import com.cursorai.remote.data.model.*
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.AppTab
import com.cursorai.remote.ui.MainViewModel
import com.cursorai.remote.ui.components.*
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

    // Tablet IDE state
    val sidebarPanel by viewModel.sidebarPanel.collectAsState()
    val sidebarVisible by viewModel.sidebarVisible.collectAsState()
    val bottomPanelTab by viewModel.bottomPanelTab.collectAsState()
    val bottomPanelVisible by viewModel.bottomPanelVisible.collectAsState()
    val openTabs by viewModel.openTabs.collectAsState()
    val activeTabIndex by viewModel.activeTabIndex.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val gitBranch by viewModel.gitBranch.collectAsState()
    val outputLines by viewModel.outputLines.collectAsState()

    val isTablet = widthSizeClass != WindowWidthSizeClass.Compact

    Box(modifier = Modifier.fillMaxSize()) {
        if (isTablet) {
            // ============================================
            // TABLET: Full Cursor IDE layout
            // ============================================
            CursorIDELayout(
                // Connection
                connectionState = connectionState,
                latency = latency,
                // Editor
                editorState = editorState,
                openTabs = openTabs,
                activeTabIndex = activeTabIndex,
                // Panels
                sidebarPanel = sidebarPanel,
                sidebarVisible = sidebarVisible,
                bottomPanelTab = bottomPanelTab,
                bottomPanelVisible = bottomPanelVisible,
                // Data
                fileTree = fileTree,
                terminalLines = terminalLines,
                chatMessages = chatMessages,
                diagnostics = diagnostics,
                outputLines = outputLines,
                gitBranch = gitBranch,
                // Voice
                voiceState = voiceState,
                // Callbacks
                viewModel = viewModel
            )
        } else {
            // ============================================
            // PHONE: Bottom navigation layout
            // ============================================
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

        // Voice overlay (both phone and tablet)
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

// ========================================================================
// CURSOR IDE LAYOUT (Tablet / Landscape)
// ========================================================================

/**
 * Full Cursor IDE replica layout:
 * ┌──────────────────────────────────────────────────────┐
 * │                    Title Bar                         │
 * ├────┬─────────┬──────────────────────────────────────┤
 * │    │         │  Tab Bar                              │
 * │ A  │ Side    │  Breadcrumbs                          │
 * │ c  │  bar    │  ┌──────────────────────┐┌──────┐   │
 * │ t  │  Panel  │  │  Code Editor         ││ Mini │   │
 * │ i  │         │  │                      ││ map  │   │
 * │ v  │         │  │                      ││      │   │
 * │ i  │         │  └──────────────────────┘└──────┘   │
 * │ t  │         ├──────────────────────────────────────┤
 * │ y  │         │  Bottom Panel (Terminal/Problems/..) │
 * │    │         │                                       │
 * ├────┴─────────┴──────────────────────────────────────┤
 * │                    Status Bar                        │
 * └──────────────────────────────────────────────────────┘
 */
@Composable
fun CursorIDELayout(
    connectionState: ConnectionState,
    latency: Long,
    editorState: EditorState,
    openTabs: List<EditorTab>,
    activeTabIndex: Int,
    sidebarPanel: SidebarPanel,
    sidebarVisible: Boolean,
    bottomPanelTab: BottomPanelTab,
    bottomPanelVisible: Boolean,
    fileTree: List<FileNode>,
    terminalLines: List<TerminalLine>,
    chatMessages: List<ChatMessage>,
    diagnostics: List<DiagnosticEntry>,
    outputLines: List<String>,
    gitBranch: String,
    voiceState: VoiceState,
    viewModel: MainViewModel
) {
    var showSettings by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Title Bar
        CursorTitleBar(
            projectName = "my-project",
            activeFileName = editorState.fileName,
            connectionState = connectionState,
            onBack = { viewModel.webSocketManager.sendCommand("workbench.action.navigateBack") },
            onForward = { viewModel.webSocketManager.sendCommand("workbench.action.navigateForward") },
            onSearch = { viewModel.webSocketManager.sendCommand("workbench.action.quickOpen") },
            onToggleSidebar = { viewModel.toggleSidebar() },
            onTogglePanel = { viewModel.toggleBottomPanel() }
        )

        // Main body: ActivityBar | Sidebar | Editor+Panel
        Row(modifier = Modifier.weight(1f)) {
            // Activity Bar (far left, 48dp)
            ActivityBar(
                activePanel = sidebarPanel,
                sidebarVisible = sidebarVisible,
                voiceState = voiceState,
                onPanelSelect = { viewModel.setSidebarPanel(it) },
                onVoiceToggle = { viewModel.toggleVoiceOverlay() },
                onSettingsClick = { showSettings = !showSettings }
            )

            if (showSettings) {
                // Settings replaces everything when active
                val connectionConfig by viewModel.connectionConfig.collectAsState()
                val voiceLanguage by viewModel.voiceLanguage.collectAsState()
                val autoConnect by viewModel.autoConnect.collectAsState()
                val hapticFeedback by viewModel.hapticFeedback.collectAsState()
                val fontSize by viewModel.fontSize.collectAsState()

                SettingsScreen(
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
                    onSaveFontSize = { viewModel.saveFontSize(it) },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Planning state
                val projectPlan by viewModel.projectPlan.collectAsState()
                val planningStep by viewModel.planningStep.collectAsState()
                val planningMessages by viewModel.planningMessages.collectAsState()

                // Sidebar (collapsible, 260dp)
                CursorSidebar(
                    visible = sidebarVisible,
                    activePanel = sidebarPanel,
                    fileTree = fileTree,
                    chatMessages = chatMessages,
                    voiceState = voiceState,
                    // Planning
                    projectPlan = projectPlan,
                    planningStep = planningStep,
                    planningMessages = planningMessages,
                    onFileSelect = { viewModel.requestFile(it) },
                    onRefresh = { viewModel.webSocketManager.requestFileTree() },
                    onSendChat = { viewModel.sendAIMessage(it) },
                    onVoiceInput = { viewModel.toggleVoiceOverlay() },
                    // Planning callbacks
                    onSendPlanningMessage = { viewModel.sendPlanningMessage(it) },
                    onSelectPlanningOption = { viewModel.selectPlanningOption(it) },
                    onStartPlanning = { viewModel.startPlanning() },
                    onFeatureToggle = { viewModel.toggleFeatureStatus(it) },
                    onSubtaskToggle = { fId, sId -> viewModel.toggleSubtask(fId, sId) }
                )

                // Editor area + Bottom panel
                Column(modifier = Modifier.weight(1f)) {
                    // Editor pane
                    CursorEditorPane(
                        editorState = editorState,
                        openTabs = openTabs,
                        activeTabIndex = activeTabIndex,
                        onTabSelect = { viewModel.setActiveTab(it) },
                        onTabClose = { viewModel.closeTab(it) },
                        onAction = { viewModel.webSocketManager.sendEditorAction(it) },
                        modifier = if (bottomPanelVisible) Modifier.weight(0.6f) else Modifier.weight(1f)
                    )

                    // Bottom Panel (with Preview & Build)
                    val previewUrl by viewModel.previewUrl.collectAsState()
                    val isDevServerRunning by viewModel.isDevServerRunning.collectAsState()
                    val consoleLogs by viewModel.consoleLogs.collectAsState()
                    val selectedDevice by viewModel.selectedDevice.collectAsState()
                    val buildState by viewModel.buildState.collectAsState()
                    val buildOutput by viewModel.buildOutput.collectAsState()

                    BottomPanel(
                        visible = bottomPanelVisible,
                        activeTab = bottomPanelTab,
                        terminalLines = terminalLines,
                        chatMessages = chatMessages,
                        diagnostics = diagnostics,
                        outputLines = outputLines,
                        voiceState = voiceState,
                        // Preview & Build
                        previewUrl = previewUrl,
                        isDevServerRunning = isDevServerRunning,
                        consoleLogs = consoleLogs,
                        selectedDevice = selectedDevice,
                        buildState = buildState,
                        buildOutput = buildOutput,
                        buildTasks = viewModel.buildTasks,
                        onTabSelect = { viewModel.setBottomPanelTab(it) },
                        onToggle = { viewModel.toggleBottomPanel() },
                        onSendTerminal = { viewModel.sendTerminalCommand(it) },
                        onSendChat = { viewModel.sendAIMessage(it) },
                        onVoiceInput = { viewModel.toggleVoiceOverlay() },
                        // Preview & Build callbacks
                        onPreviewUrlChange = { viewModel.setPreviewUrl(it) },
                        onPreviewRefresh = { },
                        onDeviceChange = { viewModel.setPreviewDevice(it) },
                        onStartDevServer = { viewModel.startDevServer() },
                        onStopDevServer = { viewModel.stopDevServer() },
                        onConsoleClear = { viewModel.clearConsoleLogs() },
                        onRunBuildTask = { viewModel.runBuildTask(it) },
                        onStopBuild = { viewModel.stopBuild() },
                        onClearBuildOutput = { viewModel.clearBuildOutput() },
                        modifier = if (bottomPanelVisible) Modifier.weight(0.4f) else Modifier
                    )
                }
            }
        }

        // Status Bar (bottom, 22dp)
        CursorStatusBar(
            connectionState = connectionState,
            gitBranch = gitBranch,
            errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR },
            warningCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING },
            line = editorState.cursorLine + 1,
            column = editorState.cursorColumn + 1,
            language = editorState.language.ifBlank { "Plain Text" },
            latency = latency
        )
    }
}


// ========================================================================
// PHONE LAYOUT (unchanged)
// ========================================================================

@Composable
fun PhoneLayout(
    currentTab: AppTab,
    connectionState: ConnectionState,
    latency: Long,
    voiceState: VoiceState,
    amplitude: Float,
    editorState: EditorState,
    fileTree: List<FileNode>,
    terminalLines: List<TerminalLine>,
    chatMessages: List<ChatMessage>,
    connectionConfig: ConnectionConfig,
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
