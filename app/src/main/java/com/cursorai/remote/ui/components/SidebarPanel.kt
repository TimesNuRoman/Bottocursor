package com.cursorai.remote.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.ChatMessage
import com.cursorai.remote.data.model.ChatRole
import com.cursorai.remote.data.model.FileNode
import com.cursorai.remote.data.model.SidebarPanel
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.theme.*

/**
 * Sidebar content panel — renders based on active SidebarPanel type.
 * Width is ~240-280dp, matching Cursor IDE sidebar.
 */
@Composable
fun CursorSidebar(
    visible: Boolean,
    activePanel: SidebarPanel,
    fileTree: List<FileNode>,
    chatMessages: List<ChatMessage>,
    voiceState: VoiceState,
    // Planning props
    projectPlan: com.cursorai.remote.data.model.ProjectPlan? = null,
    planningStep: com.cursorai.remote.data.model.PlanningStep = com.cursorai.remote.data.model.PlanningStep.WELCOME,
    planningMessages: List<ChatMessage> = emptyList(),
    onFileSelect: (String) -> Unit,
    onRefresh: () -> Unit,
    onSendChat: (String) -> Unit,
    onVoiceInput: () -> Unit,
    // Planning callbacks
    onSendPlanningMessage: (String) -> Unit = {},
    onSelectPlanningOption: (String) -> Unit = {},
    onStartPlanning: () -> Unit = {},
    onFeatureToggle: (String) -> Unit = {},
    onSubtaskToggle: (String, String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(),
        exit = shrinkHorizontally(),
        modifier = modifier
    ) {
        Row {
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(SideBarBg)
            ) {
                when (activePanel) {
                    SidebarPanel.PROJECT_PLAN -> ProjectPlanSidePanel(
                        plan = projectPlan,
                        planningStep = planningStep,
                        planningMessages = planningMessages,
                        voiceState = voiceState,
                        onSendMessage = onSendPlanningMessage,
                        onSelectOption = onSelectPlanningOption,
                        onStartPlanning = onStartPlanning,
                        onFeatureToggle = onFeatureToggle,
                        onSubtaskToggle = onSubtaskToggle,
                        onVoiceInput = onVoiceInput
                    )
                    SidebarPanel.EXPLORER -> ExplorerPanel(
                        fileTree = fileTree,
                        onFileSelect = onFileSelect,
                        onRefresh = onRefresh
                    )
                    SidebarPanel.SEARCH -> SearchPanel()
                    SidebarPanel.GIT -> GitPanel()
                    SidebarPanel.DEBUG -> DebugPanel()
                    SidebarPanel.EXTENSIONS -> ExtensionsPanel()
                    SidebarPanel.AI_CHAT -> AIChatSidePanel(
                        messages = chatMessages,
                        voiceState = voiceState,
                        onSend = onSendChat,
                        onVoiceInput = onVoiceInput
                    )
                    SidebarPanel.NONE -> {}
                }
            }

            // Right border
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(PanelBorder)
            )
        }
    }
}

@Composable
fun SidebarSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(SideBarSectionHeader)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            ),
            color = SideBarFg
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            content = actions
        )
    }
}

@Composable
fun ExplorerPanel(
    fileTree: List<FileNode>,
    onFileSelect: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SidebarSectionHeader(
            title = "EXPLORER",
            actions = {
                IconButton(onClick = { /* new file */ }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.NoteAdd, contentDescription = "New File", tint = SideBarFg, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = { /* new folder */ }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = "New Folder", tint = SideBarFg, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onRefresh, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = SideBarFg, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = { /* collapse */ }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.UnfoldLess, contentDescription = "Collapse All", tint = SideBarFg, modifier = Modifier.size(14.dp))
                }
            }
        )

        // Project name collapsible section
        SidebarCollapsibleSection(title = "MY-PROJECT", defaultExpanded = true) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(fileTree) { node ->
                    SidebarFileItem(
                        node = node,
                        depth = 0,
                        onFileSelect = onFileSelect
                    )
                }
            }
        }

        // Outline section
        SidebarCollapsibleSection(title = "OUTLINE", defaultExpanded = false) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    "No symbols found",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
            }
        }

        // Timeline section
        SidebarCollapsibleSection(title = "TIMELINE", defaultExpanded = false) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    "No timeline entries",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
fun SidebarCollapsibleSection(
    title: String,
    defaultExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(SideBarBg)
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = SideBarFg,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SideBarFg
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            content()
        }
    }
}

@Composable
fun SidebarFileItem(
    node: FileNode,
    depth: Int,
    onFileSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(depth < 1) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (node.isDirectory) expanded = !expanded
                        else onFileSelect(node.path)
                    }
                )
                .padding(start = (16 + depth * 12).dp, end = 8.dp)
        ) {
            if (node.isDirectory) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = SideBarFg,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = if (expanded) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = StatusWarning.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Spacer(Modifier.width(14.dp))
                Icon(
                    imageVector = getFileTypeIcon(node.extension),
                    contentDescription = null,
                    tint = getFileTypeColor(node.extension),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            Text(
                text = node.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = SideBarFg,
                maxLines = 1
            )
        }

        if (expanded && node.isDirectory) {
            node.children.forEach { child ->
                SidebarFileItem(node = child, depth = depth + 1, onFileSelect = onFileSelect)
            }
        }
    }
}

@Composable
fun SearchPanel() {
    Column(modifier = Modifier.fillMaxSize()) {
        SidebarSectionHeader(title = "SEARCH")

        Column(modifier = Modifier.padding(12.dp)) {
            // Search input
            BasicTextField(
                value = "",
                onValueChange = {},
                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                cursorBrush = SolidColor(CursorPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(CursorSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                decorationBox = { inner ->
                    Box {
                        Text("Search", style = TextStyle(fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )

            Spacer(Modifier.height(6.dp))

            // Replace input
            BasicTextField(
                value = "",
                onValueChange = {},
                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                cursorBrush = SolidColor(CursorPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(CursorSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                decorationBox = { inner ->
                    Box {
                        Text("Replace", style = TextStyle(fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            // Files to include / exclude
            BasicTextField(
                value = "",
                onValueChange = {},
                textStyle = TextStyle(fontSize = 11.sp, color = TextPrimary),
                cursorBrush = SolidColor(CursorPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(CursorSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                decorationBox = { inner ->
                    Box {
                        Text("files to include", style = TextStyle(fontSize = 11.sp, color = TextTertiary))
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
fun GitPanel() {
    Column(modifier = Modifier.fillMaxSize()) {
        SidebarSectionHeader(
            title = "SOURCE CONTROL",
            actions = {
                IconButton(onClick = {}, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = SideBarFg, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = {}, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.Check, contentDescription = "Commit", tint = SideBarFg, modifier = Modifier.size(14.dp))
                }
            }
        )

        // Commit message input
        Column(modifier = Modifier.padding(12.dp)) {
            BasicTextField(
                value = "",
                onValueChange = {},
                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                cursorBrush = SolidColor(CursorPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CursorSurfaceElevated)
                    .padding(8.dp),
                decorationBox = { inner ->
                    Box {
                        Text("Message (Ctrl+Enter to commit)", style = TextStyle(fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )
        }

        // Changes section
        SidebarCollapsibleSection(title = "CHANGES (0)") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text("No changes", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextTertiary)
            }
        }
    }
}

@Composable
fun DebugPanel() {
    Column(modifier = Modifier.fillMaxSize()) {
        SidebarSectionHeader(title = "RUN AND DEBUG")

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "To customize Run and Debug",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
                Text(
                    "create a launch.json file.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextLink
                )
            }
        }
    }
}

@Composable
fun ExtensionsPanel() {
    Column(modifier = Modifier.fillMaxSize()) {
        SidebarSectionHeader(title = "EXTENSIONS")

        Column(modifier = Modifier.padding(12.dp)) {
            BasicTextField(
                value = "",
                onValueChange = {},
                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                cursorBrush = SolidColor(CursorPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(CursorSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                decorationBox = { inner ->
                    Box {
                        Text("Search Extensions", style = TextStyle(fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
fun AIChatSidePanel(
    messages: List<ChatMessage>,
    voiceState: VoiceState,
    onSend: (String) -> Unit,
    onVoiceInput: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        SidebarSectionHeader(title = "CURSOR AI")

        if (messages.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = CursorPrimary.copy(alpha = 0.3f), modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Ask anything about your code", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextTertiary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages) { msg ->
                    Row {
                        if (msg.role == ChatRole.ASSISTANT) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = CursorPrimary, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = msg.content,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = if (msg.role == ChatRole.USER) TextSecondary else TextPrimary
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
                .background(SideBarSectionHeader)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                cursorBrush = SolidColor(CursorPrimary),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CursorSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                decorationBox = { inner ->
                    Box {
                        if (input.isEmpty()) Text("Ask Cursor AI...", style = TextStyle(fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onVoiceInput, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Rounded.Mic, contentDescription = "Voice", tint = TextTertiary, modifier = Modifier.size(14.dp))
            }
            if (input.isNotBlank()) {
                IconButton(onClick = { onSend(input); input = "" }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.Send, contentDescription = "Send", tint = CursorPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

private fun getFileTypeIcon(ext: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (ext.lowercase()) {
        "ts", "tsx" -> Icons.Rounded.Code
        "js", "jsx" -> Icons.Rounded.Javascript
        "json" -> Icons.Rounded.DataObject
        "md" -> Icons.Rounded.Description
        "css", "scss" -> Icons.Rounded.Palette
        "html" -> Icons.Rounded.Language
        "png", "jpg", "svg" -> Icons.Rounded.Image
        "gitignore" -> Icons.Rounded.VisibilityOff
        "ico" -> Icons.Rounded.Image
        else -> Icons.Rounded.InsertDriveFile
    }
}

private fun getFileTypeColor(ext: String): androidx.compose.ui.graphics.Color {
    return when (ext.lowercase()) {
        "ts", "tsx" -> SyntaxNumber
        "js", "jsx" -> StatusWarning
        "json" -> StatusWarning
        "md" -> SyntaxFunction
        "css", "scss" -> SyntaxKeyword
        "html" -> SyntaxKeyword
        "gitignore" -> TextTertiary
        else -> TextTertiary
    }
}
