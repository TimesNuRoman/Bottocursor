package com.cursorai.remote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.theme.*

@Composable
fun VoiceButton(
    voiceState: VoiceState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val isListening = voiceState == VoiceState.LISTENING
    val isProcessing = voiceState == VoiceState.PROCESSING

    // Pulsing animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotation for processing state
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val currentScale = when {
        isListening -> 1f + (amplitude * 0.3f)
        isProcessing -> 1f
        else -> 1f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size + 24.dp)
    ) {
        // Outer pulse ring (when listening)
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(size + 20.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                VoicePulse,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Amplitude ring
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(size + 8.dp)
                    .scale(currentScale)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(VoiceActiveStart, VoiceActiveEnd)
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Main button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = if (isListening) 12.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = CursorPrimary.copy(alpha = 0.3f),
                    spotColor = CursorPrimary.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(
                    brush = if (isListening) {
                        Brush.linearGradient(
                            colors = listOf(VoiceActiveStart, VoiceActiveEnd)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(CursorSurfaceElevated, CursorSurfaceHigh)
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isListening) Color.Transparent else BorderDefault,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Icon(
                imageVector = when {
                    isListening -> Icons.Rounded.Stop
                    isProcessing -> Icons.Rounded.Mic
                    else -> Icons.Rounded.Mic
                },
                contentDescription = "Voice input",
                tint = if (isListening) Color.White else TextSecondary,
                modifier = Modifier.size(size * 0.4f)
            )
        }
    }
}

@Composable
fun MiniVoiceButton(
    voiceState: VoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = voiceState == VoiceState.LISTENING

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
            contentDescription = "Voice input",
            tint = if (isListening) CursorPrimary else TextSecondary
        )
    }
}
