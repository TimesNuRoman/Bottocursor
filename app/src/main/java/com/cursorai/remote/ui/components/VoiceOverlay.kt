package com.cursorai.remote.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.QuickAction
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.theme.*

@Composable
fun VoiceOverlay(
    visible: Boolean,
    voiceState: VoiceState,
    amplitude: Float,
    partialText: String,
    commandHistory: List<String>,
    quickActions: List<QuickAction>,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onQuickAction: (QuickAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { it },
        exit = fadeOut(tween(200)) + slideOutVertically(tween(300)) { it },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            CursorSurface.copy(alpha = 0.97f),
                            CursorSurface
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* consume click */ }
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.weight(0.3f))

                // Status text
                Text(
                    text = when (voiceState) {
                        VoiceState.LISTENING -> "Listening..."
                        VoiceState.PROCESSING -> "Processing..."
                        VoiceState.ERROR -> "Try again"
                        VoiceState.IDLE -> "Tap to speak"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Say a command or ask AI anything",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(Modifier.height(32.dp))

                // Waveform visualization
                if (voiceState == VoiceState.LISTENING) {
                    WaveformVisualizer(
                        amplitude = amplitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Partial recognized text
                if (partialText.isNotBlank()) {
                    Text(
                        text = partialText,
                        style = MaterialTheme.typography.titleLarge,
                        color = CursorPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // Voice button
                VoiceButton(
                    voiceState = voiceState,
                    amplitude = amplitude,
                    onClick = {
                        if (voiceState == VoiceState.LISTENING) {
                            onStopListening()
                        } else {
                            onStartListening()
                        }
                    },
                    size = 80.dp
                )

                Spacer(Modifier.height(32.dp))

                // Quick actions
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )

                Spacer(Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(quickActions) { action ->
                        QuickActionChip(
                            action = action,
                            onClick = { onQuickAction(action) }
                        )
                    }
                }

                Spacer(Modifier.weight(0.3f))

                // Recent commands
                if (commandHistory.isNotEmpty()) {
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    commandHistory.take(3).forEach { cmd ->
                        Text(
                            text = cmd,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickActionChip(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = CursorSurfaceElevated,
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = modifier
    ) {
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun WaveformVisualizer(
    amplitude: Float,
    modifier: Modifier = Modifier,
    barCount: Int = 32
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(barCount) { index ->
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600 + (index * 50),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )

            val barHeight = (0.2f + (amplitude * phase * 0.8f)).coerceIn(0.1f, 1f)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(VoiceActiveStart, VoiceActiveEnd)
                        )
                    )
            )
        }
    }
}
