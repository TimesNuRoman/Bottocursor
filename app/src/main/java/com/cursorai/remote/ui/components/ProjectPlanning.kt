package com.cursorai.remote.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.*
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.theme.*

// =========================================================================
// Planning Sidebar Panel — shown in the sidebar when PROJECT_PLAN is active
// =========================================================================

@Composable
fun ProjectPlanSidePanel(
    plan: ProjectPlan?,
    planningStep: PlanningStep,
    planningMessages: List<ChatMessage>,
    voiceState: VoiceState,
    onSendMessage: (String) -> Unit,
    onSelectOption: (String) -> Unit,
    onStartPlanning: () -> Unit,
    onFeatureToggle: (String) -> Unit,
    onSubtaskToggle: (String, String) -> Unit,
    onVoiceInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SidebarSectionHeader(title = "PROJECT PLAN", actions = {
            if (plan != null && plan.isActive) {
                IconButton(onClick = { /* edit plan */ }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Rounded.Edit, "Edit", tint = SideBarFg, modifier = Modifier.size(14.dp))
                }
            }
        })

        if (plan == null || planningStep != PlanningStep.ACTIVE) {
            // Planning wizard / chat mode
            PlanningWizard(
                step = planningStep,
                messages = planningMessages,
                voiceState = voiceState,
                onSend = onSendMessage,
                onSelectOption = onSelectOption,
                onStart = onStartPlanning,
                onVoiceInput = onVoiceInput
            )
        } else {
            // Active plan view
            ActivePlanView(
                plan = plan,
                onFeatureToggle = onFeatureToggle,
                onSubtaskToggle = onSubtaskToggle
            )
        }
    }
}

// =========================================================================
// Planning Wizard — the interactive Q&A flow
// =========================================================================

@Composable
fun PlanningWizard(
    step: PlanningStep,
    messages: List<ChatMessage>,
    voiceState: VoiceState,
    onSend: (String) -> Unit,
    onSelectOption: (String) -> Unit,
    onStart: () -> Unit,
    onVoiceInput: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Step indicator
        PlanningStepIndicator(currentStep = step)

        if (step == PlanningStep.WELCOME && messages.isEmpty()) {
            // Welcome screen
            WelcomeScreen(onStart = onStart)
        } else {
            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(messages) { msg ->
                    PlanningMessage(
                        message = msg,
                        onSelectOption = onSelectOption
                    )
                }
            }

            // Input area
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
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CursorSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    decorationBox = { inner ->
                        Box {
                            if (input.isEmpty()) Text(
                                "Describe your project...",
                                style = TextStyle(fontSize = 12.sp, color = TextTertiary)
                            )
                            inner()
                        }
                    }
                )
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onVoiceInput, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Rounded.Mic,
                        contentDescription = "Voice",
                        tint = if (voiceState == VoiceState.LISTENING) CursorPrimary else TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (input.isNotBlank()) {
                    IconButton(onClick = { onSend(input); input = "" }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Send, "Send", tint = CursorPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PlanningStepIndicator(currentStep: PlanningStep) {
    val steps = listOf(
        PlanningStep.WELCOME to "Start",
        PlanningStep.REQUIREMENTS to "Req.",
        PlanningStep.TECH_STACK to "Tech",
        PlanningStep.ARCHITECTURE to "Arch.",
        PlanningStep.FEATURES to "Features",
        PlanningStep.MILESTONES to "Plan",
        PlanningStep.REVIEW to "Review",
    )
    val currentIdx = steps.indexOfFirst { it.first == currentStep }.coerceAtLeast(0)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(SideBarSectionHeader)
            .padding(horizontal = 8.dp)
    ) {
        steps.forEachIndexed { index, (_, label) ->
            val isDone = index < currentIdx
            val isCurrent = index == currentIdx
            val color = when {
                isDone -> StatusSuccess
                isCurrent -> CursorPrimary
                else -> TextTertiary.copy(alpha = 0.4f)
            }

            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(1.dp)
                        .background(if (isDone) StatusSuccess else PanelBorder)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) color else Color.Transparent)
                        .border(1.dp, color, CircleShape)
                ) {
                    if (isDone) {
                        Icon(Icons.Rounded.Check, null, tint = color, modifier = Modifier.size(9.dp))
                    } else if (isCurrent) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(Color.White))
                    }
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = color
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Animated icon
        val rotation by rememberInfiniteTransition(label = "glow").animateFloat(
            0f, 360f,
            infiniteRepeatable(tween(8000, easing = LinearEasing)),
            label = "rot"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CursorPrimary.copy(alpha = 0.15f))
        ) {
            Icon(
                Icons.Rounded.RocketLaunch,
                contentDescription = null,
                tint = CursorPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "New Project",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "I'll help you plan your project\nstep by step",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Project type chips
        val types = listOf(
            ProjectType.WEB_APP, ProjectType.MOBILE_APP, ProjectType.API_BACKEND,
            ProjectType.FULL_STACK, ProjectType.CLI_TOOL, ProjectType.OTHER
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            types.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { type ->
                        Surface(
                            onClick = onStart,
                            shape = RoundedCornerShape(8.dp),
                            color = CursorSurfaceElevated,
                            border = BorderStroke(1.dp, PanelBorder),
                            modifier = Modifier.width(100.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(type.emoji, fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    type.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = CursorPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Start Planning", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PlanningMessage(
    message: ChatMessage,
    onSelectOption: (String) -> Unit,
) {
    val isUser = message.role == ChatRole.USER
    val isSystem = message.role == ChatRole.SYSTEM

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isUser) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CursorPrimary.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = CursorPrimary, modifier = Modifier.size(12.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = TextPrimary
                )
            }

            // Parse options from message content (lines starting with "- ")
            val options = message.content.lines()
                .filter { it.trimStart().startsWith("• ") || it.trimStart().startsWith("- ") }
                .map { it.trimStart().removePrefix("• ").removePrefix("- ").trim() }
                .filter { it.isNotBlank() }

            if (options.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 26.dp)
                ) {
                    options.forEach { option ->
                        Surface(
                            onClick = { onSelectOption(option) },
                            shape = RoundedCornerShape(6.dp),
                            color = CursorSurfaceElevated,
                            border = BorderStroke(1.dp, PanelBorder),
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // User message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 200.dp)
                        .clip(RoundedCornerShape(10.dp, 10.dp, 2.dp, 10.dp))
                        .background(CursorPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// =========================================================================
// Active Plan View — shows the finalized plan with progress tracking
// =========================================================================

@Composable
fun ActivePlanView(
    plan: ProjectPlan,
    onFeatureToggle: (String) -> Unit,
    onSubtaskToggle: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Project header
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    plan.name.ifBlank { "My Project" },
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (plan.description.isNotBlank()) {
                    Text(
                        plan.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Progress bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { plan.completionPercent / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = CursorPrimary,
                        trackColor = CursorSurfaceElevated,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${plan.completionPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = CursorPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Tech stack badges
                if (plan.techStack.framework.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOfNotNull(
                            plan.techStack.language.takeIf { it.isNotBlank() },
                            plan.techStack.framework.takeIf { it.isNotBlank() },
                            plan.techStack.database.takeIf { it.isNotBlank() }
                        ).forEach { tech ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CursorPrimary.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    tech,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = CursorPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Phases
        plan.phases.forEach { phase ->
            item {
                SidebarCollapsibleSection(
                    title = "PHASE ${phase.order + 1}: ${phase.name.uppercase()}",
                    defaultExpanded = !phase.isComplete
                ) {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        plan.features
                            .filter { it.phaseId == phase.id }
                            .forEach { feature ->
                                FeatureItem(
                                    feature = feature,
                                    onToggle = { onFeatureToggle(feature.id) },
                                    onSubtaskToggle = { subtaskId -> onSubtaskToggle(feature.id, subtaskId) }
                                )
                            }
                    }
                }
            }
        }

        // Unassigned features
        val unassigned = plan.features.filter { f -> plan.phases.none { p -> f.phaseId == p.id } }
        if (unassigned.isNotEmpty()) {
            item {
                SidebarCollapsibleSection(title = "BACKLOG", defaultExpanded = true) {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        unassigned.forEach { feature ->
                            FeatureItem(
                                feature = feature,
                                onToggle = { onFeatureToggle(feature.id) },
                                onSubtaskToggle = { subtaskId -> onSubtaskToggle(feature.id, subtaskId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    feature: Feature,
    onToggle: () -> Unit,
    onSubtaskToggle: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable { expanded = !expanded }
                .padding(vertical = 3.dp, horizontal = 2.dp)
        ) {
            // Status checkbox
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(
                        1.dp,
                        when (feature.status) {
                            FeatureStatus.DONE -> StatusSuccess
                            FeatureStatus.IN_PROGRESS -> CursorPrimary
                            FeatureStatus.BLOCKED -> StatusError
                            else -> TextTertiary
                        },
                        RoundedCornerShape(3.dp)
                    )
                    .background(
                        if (feature.status == FeatureStatus.DONE) StatusSuccess.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
            ) {
                if (feature.status == FeatureStatus.DONE) {
                    Icon(Icons.Rounded.Check, null, tint = StatusSuccess, modifier = Modifier.size(11.dp))
                }
            }

            Spacer(Modifier.width(6.dp))

            // Priority dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        when (feature.priority) {
                            Priority.MUST -> StatusError
                            Priority.SHOULD -> StatusWarning
                            Priority.COULD -> StatusInfo
                            Priority.WONT -> TextTertiary
                        }
                    )
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = feature.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    textDecoration = if (feature.status == FeatureStatus.DONE) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (feature.status == FeatureStatus.DONE) TextTertiary else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Subtask count
            if (feature.subtasks.isNotEmpty()) {
                val done = feature.subtasks.count { it.isDone }
                Text(
                    "$done/${feature.subtasks.size}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }

            // Expand arrow
            if (feature.subtasks.isNotEmpty() || feature.description.isNotBlank()) {
                Icon(
                    if (expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowRight,
                    null, tint = TextTertiary, modifier = Modifier.size(14.dp)
                )
            }
        }

        // Expanded content
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 4.dp)) {
                if (feature.description.isNotBlank()) {
                    Text(
                        feature.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 15.sp),
                        color = TextTertiary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                feature.subtasks.forEach { subtask ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clickable { onSubtaskToggle(subtask.id) }
                    ) {
                        Checkbox(
                            checked = subtask.isDone,
                            onCheckedChange = { onSubtaskToggle(subtask.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = StatusSuccess,
                                uncheckedColor = TextTertiary.copy(alpha = 0.5f),
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            subtask.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                textDecoration = if (subtask.isDone) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (subtask.isDone) TextTertiary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
