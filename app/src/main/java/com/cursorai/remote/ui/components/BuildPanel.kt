package com.cursorai.remote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.BuildState
import com.cursorai.remote.data.model.BuildTask
import com.cursorai.remote.ui.theme.*

/**
 * Build panel — shows build tasks, output, and quick-run actions.
 */
@Composable
fun BuildPanel(
    buildState: BuildState,
    buildOutput: List<String>,
    buildTasks: List<BuildTask>,
    devServerRunning: Boolean,
    onRunTask: (BuildTask) -> Unit,
    onStopBuild: () -> Unit,
    onClearOutput: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(buildOutput.size) {
        if (buildOutput.isNotEmpty()) listState.animateScrollToItem(buildOutput.size - 1)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Build toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(PanelBg)
                .padding(horizontal = 10.dp)
        ) {
            // Build state indicator
            when (buildState) {
                BuildState.BUILDING -> {
                    val rotation by rememberInfiniteTransition(label = "spin").animateFloat(
                        initialValue = 0f, targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
                        label = "spin"
                    )
                    Icon(
                        Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = StatusWarning,
                        modifier = Modifier.size(13.dp).rotate(rotation)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Building...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = StatusWarning)
                }
                BuildState.SUCCESS -> {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Build succeeded", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = StatusSuccess)
                }
                BuildState.FAILED -> {
                    Icon(Icons.Rounded.Cancel, contentDescription = null, tint = StatusError, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Build failed", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = StatusError)
                }
                BuildState.IDLE -> {
                    Icon(Icons.Outlined.Build, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ready", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextTertiary)
                }
            }

            Spacer(Modifier.weight(1f))

            // Quick task buttons
            buildTasks.take(4).forEach { task ->
                QuickBuildButton(
                    task = task,
                    isRunning = buildState == BuildState.BUILDING,
                    devServerRunning = devServerRunning && task.isDevServer,
                    onClick = {
                        if (buildState == BuildState.BUILDING) onStopBuild()
                        else onRunTask(task)
                    }
                )
            }

            // Clear
            IconButton(onClick = onClearOutput, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.Delete, "Clear", tint = PanelTabInactive, modifier = Modifier.size(12.dp))
            }
        }

        // Task list (collapsible)
        if (buildOutput.isEmpty()) {
            // Show task selector
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    "Build Tasks",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                buildTasks.forEach { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onRunTask(task) }
                            .background(CursorSurfaceElevated)
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            if (task.isDevServer) Icons.Rounded.Dns else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = if (task.isDevServer) CursorSecondary else StatusSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                task.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            task.command,
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            color = TextTertiary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        } else {
            // Build output
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                items(buildOutput) { line ->
                    Text(
                        text = line,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = when {
                                line.contains("error", ignoreCase = true) -> StatusError
                                line.contains("warning", ignoreCase = true) -> StatusWarning
                                line.contains("success", ignoreCase = true) || line.contains("✓") || line.contains("ready") -> StatusSuccess
                                line.startsWith("$") -> TerminalCommand
                                else -> TerminalText
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun QuickBuildButton(
    task: BuildTask,
    isRunning: Boolean,
    devServerRunning: Boolean,
    onClick: () -> Unit,
) {
    val color = when {
        devServerRunning -> StatusError
        task.isDevServer -> CursorSecondary
        isRunning -> StatusWarning
        else -> StatusSuccess
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp)
        ) {
            Icon(
                imageVector = when {
                    devServerRunning -> Icons.Rounded.Stop
                    isRunning -> Icons.Rounded.Stop
                    task.isDevServer -> Icons.Rounded.Dns
                    else -> Icons.Rounded.PlayArrow
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(10.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = if (devServerRunning) "Stop" else task.name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                color = color
            )
        }
    }
}
