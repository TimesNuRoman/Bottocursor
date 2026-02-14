package com.cursorai.remote.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.*
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.theme.*

/**
 * Bottom Panel — Terminal/Problems/Output/Preview/Build tabs, just like Cursor IDE.
 */
@Composable
fun BottomPanel(
    visible: Boolean,
    activeTab: BottomPanelTab,
    terminalLines: List<TerminalLine>,
    chatMessages: List<ChatMessage>,
    diagnostics: List<DiagnosticEntry>,
    outputLines: List<String>,
    voiceState: VoiceState,
    // Preview & Build props
    previewUrl: String = "",
    isDevServerRunning: Boolean = false,
    consoleLogs: List<com.cursorai.remote.data.model.ConsoleLogEntry> = emptyList(),
    selectedDevice: com.cursorai.remote.data.model.PreviewDevice = com.cursorai.remote.data.model.PreviewDevice.RESPONSIVE,
    buildState: com.cursorai.remote.data.model.BuildState = com.cursorai.remote.data.model.BuildState.IDLE,
    buildOutput: List<String> = emptyList(),
    buildTasks: List<com.cursorai.remote.data.model.BuildTask> = emptyList(),
    onTabSelect: (BottomPanelTab) -> Unit,
    onToggle: () -> Unit,
    onSendTerminal: (String) -> Unit,
    onSendChat: (String) -> Unit,
    onVoiceInput: () -> Unit,
    // Preview & Build callbacks
    onPreviewUrlChange: (String) -> Unit = {},
    onPreviewRefresh: () -> Unit = {},
    onDeviceChange: (com.cursorai.remote.data.model.PreviewDevice) -> Unit = {},
    onStartDevServer: () -> Unit = {},
    onStopDevServer: () -> Unit = {},
    onConsoleClear: () -> Unit = {},
    onRunBuildTask: (com.cursorai.remote.data.model.BuildTask) -> Unit = {},
    onStopBuild: () -> Unit = {},
    onClearBuildOutput: () -> Unit = {},
    // Deploy (Cloudflare) props
    deployState: com.cursorai.remote.data.model.DeployState = com.cursorai.remote.data.model.DeployState.IDLE,
    deployOutput: List<String> = emptyList(),
    deployTargets: List<com.cursorai.remote.data.model.DeployTarget> = emptyList(),
    lastDeployment: com.cursorai.remote.data.model.DeploymentInfo? = null,
    cfProject: com.cursorai.remote.data.model.CloudflareProject? = null,
    onDeploy: (com.cursorai.remote.data.model.DeployTarget) -> Unit = {},
    onStopDeploy: () -> Unit = {},
    onClearDeployOutput: () -> Unit = {},
    onInitWrangler: () -> Unit = {},
    onOpenDeployUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PanelBorder)
            )

            // Panel tab header
            PanelTabHeader(
                activeTab = activeTab,
                diagnosticCount = diagnostics.size,
                errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR },
                warningCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING },
                onTabSelect = onTabSelect,
                onClose = onToggle
            )

            // Panel content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(PanelBg)
            ) {
                when (activeTab) {
                    BottomPanelTab.TERMINAL -> PanelTerminal(
                        lines = terminalLines,
                        voiceState = voiceState,
                        onSend = onSendTerminal,
                        onVoiceInput = onVoiceInput
                    )
                    BottomPanelTab.PROBLEMS -> PanelProblems(diagnostics = diagnostics)
                    BottomPanelTab.OUTPUT -> PanelOutput(lines = outputLines)
                    BottomPanelTab.AI_CHAT -> PanelAIChat(
                        messages = chatMessages,
                        voiceState = voiceState,
                        onSend = onSendChat,
                        onVoiceInput = onVoiceInput
                    )
                    BottomPanelTab.DEBUG_CONSOLE -> PanelOutput(
                        lines = listOf("Debug console ready.")
                    )
                    BottomPanelTab.PREVIEW -> WebPreviewPanel(
                        url = previewUrl,
                        isDevServerRunning = isDevServerRunning,
                        consoleLogs = consoleLogs,
                        selectedDevice = selectedDevice,
                        onUrlChange = onPreviewUrlChange,
                        onRefresh = onPreviewRefresh,
                        onDeviceChange = onDeviceChange,
                        onStartDevServer = onStartDevServer,
                        onStopDevServer = onStopDevServer,
                        onConsoleClear = onConsoleClear
                    )
                    BottomPanelTab.BUILD -> BuildPanel(
                        buildState = buildState,
                        buildOutput = buildOutput,
                        buildTasks = buildTasks,
                        devServerRunning = isDevServerRunning,
                        onRunTask = onRunBuildTask,
                        onStopBuild = onStopBuild,
                        onClearOutput = onClearBuildOutput
                    )
                    BottomPanelTab.DEPLOY -> CloudflareDeployPanel(
                        deployState = deployState,
                        deployOutput = deployOutput,
                        deployTargets = deployTargets,
                        lastDeployment = lastDeployment,
                        cfProject = cfProject,
                        onDeploy = onDeploy,
                        onStopDeploy = onStopDeploy,
                        onClearOutput = onClearDeployOutput,
                        onInitWrangler = onInitWrangler,
                        onOpenUrl = onOpenDeployUrl
                    )
                }
            }
        }
    }
}

@Composable
fun PanelTabHeader(
    activeTab: BottomPanelTab,
    diagnosticCount: Int,
    errorCount: Int,
    warningCount: Int,
    onTabSelect: (BottomPanelTab) -> Unit,
    onClose: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .background(PanelHeaderBg)
            .padding(horizontal = 8.dp)
    ) {
        PanelTab("PROBLEMS", BottomPanelTab.PROBLEMS, activeTab, onTabSelect,
            badge = if (diagnosticCount > 0) "$diagnosticCount" else null)
        PanelTab("OUTPUT", BottomPanelTab.OUTPUT, activeTab, onTabSelect)
        PanelTab("TERMINAL", BottomPanelTab.TERMINAL, activeTab, onTabSelect)
        PanelTab("PREVIEW", BottomPanelTab.PREVIEW, activeTab, onTabSelect,
            tintActive = CursorSecondary)
        PanelTab("BUILD", BottomPanelTab.BUILD, activeTab, onTabSelect,
            tintActive = StatusSuccess)
        PanelTab("DEPLOY", BottomPanelTab.DEPLOY, activeTab, onTabSelect,
            tintActive = Color(0xFFF6821F))  // Cloudflare orange
        PanelTab("AI CHAT", BottomPanelTab.AI_CHAT, activeTab, onTabSelect,
            tintActive = CursorPrimary)
        PanelTab("DEBUG", BottomPanelTab.DEBUG_CONSOLE, activeTab, onTabSelect)

        Spacer(Modifier.weight(1f))

        // Panel action buttons
        IconButton(
            onClick = { /* maximize */ },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Outlined.OpenInFull,
                contentDescription = "Maximize",
                tint = PanelTabInactive,
                modifier = Modifier.size(13.dp)
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Close",
                tint = PanelTabInactive,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
fun PanelTab(
    title: String,
    tab: BottomPanelTab,
    activeTab: BottomPanelTab,
    onSelect: (BottomPanelTab) -> Unit,
    badge: String? = null,
    tintActive: Color = PanelTabActive,
) {
    val isActive = tab == activeTab

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onSelect(tab) }
            )
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .height(34.dp)
                .padding(top = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Normal else FontWeight.Normal,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isActive) tintActive else PanelTabInactive
                )
                if (badge != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StatusError.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = StatusError
                        )
                    }
                }
            }
        }
        // Active indicator line
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(if (isActive) PanelTabIndicator else Color.Transparent)
        )
    }
}

@Composable
fun PanelTerminal(
    lines: List<TerminalLine>,
    voiceState: VoiceState,
    onSend: (String) -> Unit,
    onVoiceInput: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Terminal header with shell tabs
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(PanelBg)
                .padding(horizontal = 12.dp)
        ) {
            // Shell tab
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Rounded.Terminal, contentDescription = null, tint = StatusBarFg, modifier = Modifier.size(12.dp))
                Text(
                    "bash",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = PanelTabActive
                )
            }

            Spacer(Modifier.weight(1f))

            // Terminal actions
            IconButton(onClick = { /* split */ }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Outlined.SplitScreen, contentDescription = "Split", tint = PanelTabInactive, modifier = Modifier.size(12.dp))
            }
            IconButton(onClick = { /* new */ }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = "New", tint = PanelTabInactive, modifier = Modifier.size(12.dp))
            }
            IconButton(onClick = { /* kill */ }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.Delete, contentDescription = "Kill", tint = PanelTabInactive, modifier = Modifier.size(12.dp))
            }
        }

        // Terminal output
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            items(lines) { line ->
                Text(
                    text = line.text,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = when {
                            line.isCommand -> TerminalCommand
                            line.text.startsWith("Error") || line.text.startsWith("error") -> TerminalError
                            line.text.contains("➜") -> TerminalPrompt
                            else -> TerminalText
                        },
                        fontWeight = if (line.isCommand) FontWeight.Medium else FontWeight.Normal
                    ),
                    modifier = Modifier.padding(vertical = 0.dp)
                )
            }
        }

        // Input row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                "$ ",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TerminalPrompt, fontWeight = FontWeight.Bold)
            )
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TerminalCommand),
                cursorBrush = SolidColor(TerminalCommand),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (input.isEmpty()) Text("", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )

            // Voice mic
            if (voiceState != VoiceState.LISTENING) {
                IconButton(onClick = onVoiceInput, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Rounded.Mic, contentDescription = "Voice", tint = TextTertiary, modifier = Modifier.size(13.dp))
                }
            }

            // Send
            if (input.isNotBlank()) {
                IconButton(
                    onClick = { onSend(input); input = "" },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Rounded.KeyboardReturn, contentDescription = "Send", tint = CursorPrimary, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

@Composable
fun PanelProblems(diagnostics: List<DiagnosticEntry>) {
    if (diagnostics.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                "No problems detected.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            items(diagnostics) { diag ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = when (diag.severity) {
                            DiagnosticSeverity.ERROR -> Icons.Rounded.Cancel
                            DiagnosticSeverity.WARNING -> Icons.Rounded.Warning
                            DiagnosticSeverity.INFO -> Icons.Rounded.Info
                            DiagnosticSeverity.HINT -> Icons.Rounded.Lightbulb
                        },
                        contentDescription = null,
                        tint = when (diag.severity) {
                            DiagnosticSeverity.ERROR -> StatusError
                            DiagnosticSeverity.WARNING -> StatusWarning
                            DiagnosticSeverity.INFO -> StatusInfo
                            DiagnosticSeverity.HINT -> CursorSecondary
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = diag.message,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${diag.file}:${diag.line}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
fun PanelOutput(lines: List<String>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        items(lines) { line ->
            Text(
                text = line,
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp, color = TerminalText)
            )
        }
    }
}

@Composable
fun PanelAIChat(
    messages: List<ChatMessage>,
    voiceState: VoiceState,
    onSend: (String) -> Unit,
    onVoiceInput: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (messages.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = CursorPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ask Cursor AI anything",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == ChatRole.USER
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (!isUser) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = CursorPrimary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            text = msg.content,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            color = if (isUser) TextSecondary else TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(PanelHeaderBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                cursorBrush = SolidColor(CursorPrimary),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (input.isEmpty()) Text("Ask Cursor AI...", style = TextStyle(fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )
            IconButton(onClick = onVoiceInput, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.Mic, contentDescription = "Voice", tint = TextTertiary, modifier = Modifier.size(13.dp))
            }
            if (input.isNotBlank()) {
                IconButton(onClick = { onSend(input); input = "" }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send", tint = CursorPrimary, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}
