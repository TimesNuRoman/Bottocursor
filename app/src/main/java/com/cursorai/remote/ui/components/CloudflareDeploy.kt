package com.cursorai.remote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.*
import com.cursorai.remote.ui.theme.*

// Cloudflare brand color
val CloudflareOrange = Color(0xFFF6821F)
val CloudflareDark = Color(0xFF1B1B1B)
val WorkersYellow = Color(0xFFFBBF24)
val PagesBlue = Color(0xFF3B82F6)
val R2Purple = Color(0xFF8B5CF6)
val D1Cyan = Color(0xFF06B6D4)
val KVGreen = Color(0xFF10B981)

@Composable
fun CloudflareDeployPanel(
    deployState: DeployState,
    deployOutput: List<String>,
    deployTargets: List<DeployTarget>,
    lastDeployment: DeploymentInfo?,
    cfProject: CloudflareProject?,
    onDeploy: (DeployTarget) -> Unit,
    onStopDeploy: () -> Unit,
    onClearOutput: () -> Unit,
    onInitWrangler: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(deployOutput.size) {
        if (deployOutput.isNotEmpty()) listState.animateScrollToItem(deployOutput.size - 1)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Deploy toolbar with Cloudflare branding
        DeployToolbar(
            deployState = deployState,
            cfProject = cfProject,
            lastDeployment = lastDeployment,
            onStopDeploy = onStopDeploy,
            onClearOutput = onClearOutput,
            onOpenUrl = onOpenUrl
        )

        if (deployOutput.isEmpty()) {
            // Show deploy targets / initial state
            DeployTargetSelector(
                targets = deployTargets,
                cfProject = cfProject,
                lastDeployment = lastDeployment,
                deployState = deployState,
                onDeploy = onDeploy,
                onInitWrangler = onInitWrangler,
                onOpenUrl = onOpenUrl
            )
        } else {
            // Deploy output log
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                items(deployOutput) { line ->
                    Text(
                        text = line,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = when {
                                line.contains("error", ignoreCase = true) || line.contains("✘") -> StatusError
                                line.contains("warning", ignoreCase = true) -> StatusWarning
                                line.contains("success", ignoreCase = true) || line.contains("✓") ||
                                        line.contains("Published") || line.contains("deployed") -> StatusSuccess
                                line.contains("https://") -> CloudflareOrange
                                line.startsWith("$") -> TerminalCommand
                                line.contains("⛅") || line.contains("Cloudflare") -> CloudflareOrange
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
fun DeployToolbar(
    deployState: DeployState,
    cfProject: CloudflareProject?,
    lastDeployment: DeploymentInfo?,
    onStopDeploy: () -> Unit,
    onClearOutput: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .background(PanelBg)
            .padding(horizontal = 10.dp)
    ) {
        // CF Logo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CloudflareOrange.copy(alpha = 0.15f))
        ) {
            Text("⛅", fontSize = 10.sp)
        }
        Spacer(Modifier.width(6.dp))

        // State
        when (deployState) {
            DeployState.DEPLOYING -> {
                val rot by rememberInfiniteTransition(label = "d").animateFloat(
                    0f, 360f,
                    infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "r"
                )
                Icon(Icons.Rounded.Sync, null, tint = CloudflareOrange, modifier = Modifier.size(12.dp).rotate(rot))
                Spacer(Modifier.width(4.dp))
                Text("Deploying...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = CloudflareOrange)
            }
            DeployState.SUCCESS -> {
                Icon(Icons.Rounded.CheckCircle, null, tint = StatusSuccess, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Deployed", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = StatusSuccess)
                if (lastDeployment?.url?.isNotBlank() == true) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        onClick = { onOpenUrl(lastDeployment.url) },
                        shape = RoundedCornerShape(3.dp),
                        color = StatusSuccess.copy(alpha = 0.1f),
                        modifier = Modifier.height(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp)
                        ) {
                            Icon(Icons.Rounded.OpenInNew, null, tint = StatusSuccess, modifier = Modifier.size(9.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                lastDeployment.url.removePrefix("https://"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = StatusSuccess,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            DeployState.FAILED -> {
                Icon(Icons.Rounded.Cancel, null, tint = StatusError, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Deploy failed", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = StatusError)
            }
            DeployState.IDLE -> {
                Text(
                    cfProject?.name?.takeIf { it.isNotBlank() } ?: "Cloudflare Workers",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextSecondary
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (deployState == DeployState.DEPLOYING) {
            IconButton(onClick = onStopDeploy, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.Stop, "Stop", tint = StatusError, modifier = Modifier.size(12.dp))
            }
        }
        IconButton(onClick = onClearOutput, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Rounded.Delete, "Clear", tint = PanelTabInactive, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun DeployTargetSelector(
    targets: List<DeployTarget>,
    cfProject: CloudflareProject?,
    lastDeployment: DeploymentInfo?,
    deployState: DeployState,
    onDeploy: (DeployTarget) -> Unit,
    onInitWrangler: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Last deployment card
        if (lastDeployment != null && lastDeployment.url.isNotBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusSuccess.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Last Deployment", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = StatusSuccess)
                        }
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            onClick = { onOpenUrl(lastDeployment.url) },
                            shape = RoundedCornerShape(4.dp),
                            color = CursorSurfaceElevated
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.Link, null, tint = TextLink, modifier = Modifier.size(11.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    lastDeployment.url,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = TextLink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (lastDeployment.workerName.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Worker: ${lastDeployment.workerName}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                                Text("Env: ${lastDeployment.environment}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                                if (lastDeployment.size.isNotBlank()) Text("Size: ${lastDeployment.size}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                            }
                        }
                    }
                }
            }
        }

        // No wrangler.toml warning
        if (cfProject?.hasWranglerToml == false) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusWarning.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Warning, null, tint = StatusWarning, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("No wrangler.toml found", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = StatusWarning)
                        }
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = onInitWrangler,
                            colors = ButtonDefaults.buttonColors(containerColor = CloudflareOrange),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Initialize wrangler.toml", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }
                }
            }
        }

        // Section: Deploy targets
        item {
            Text(
                "Deploy Targets",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        items(targets) { target ->
            val color = when (target.icon) {
                "workers" -> WorkersYellow
                "pages" -> PagesBlue
                "r2" -> R2Purple
                "d1" -> D1Cyan
                "kv" -> KVGreen
                else -> CloudflareOrange
            }
            Surface(
                onClick = { onDeploy(target) },
                shape = RoundedCornerShape(6.dp),
                color = CursorSurfaceElevated,
                border = BorderStroke(1.dp, PanelBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    // Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            when (target.icon) {
                                "workers" -> Icons.Rounded.Memory
                                "pages" -> Icons.Rounded.Language
                                "r2" -> Icons.Rounded.Storage
                                "d1" -> Icons.Rounded.TableChart
                                "kv" -> Icons.Rounded.DataObject
                                "tail" -> Icons.Rounded.BugReport
                                else -> Icons.Rounded.CloudUpload
                            },
                            null, tint = color, modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            target.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                            color = TextPrimary
                        )
                        Text(
                            target.description,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextTertiary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        target.command.removePrefix("npx wrangler "),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                        color = TextTertiary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Rounded.PlayArrow, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                }
            }
        }

        // Wrangler docs hint
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Icon(Icons.Rounded.Info, null, tint = TextTertiary.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Requires wrangler CLI. Run: npm i -D wrangler",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }
        }
    }
}
