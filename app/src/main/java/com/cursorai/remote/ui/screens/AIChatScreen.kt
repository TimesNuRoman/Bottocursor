package com.cursorai.remote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.ChatMessage
import com.cursorai.remote.data.model.ChatRole
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.components.MiniVoiceButton
import com.cursorai.remote.ui.theme.*

@Composable
fun AIChatScreen(
    messages: List<ChatMessage>,
    voiceState: VoiceState,
    onSendMessage: (String) -> Unit,
    onVoiceInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CursorSurface)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(CursorSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = CursorPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Cursor AI Chat",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Ask anything about your code",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        // Messages
        if (messages.isEmpty()) {
            EmptyChatState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
            }
        }

        // Input area
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .background(CursorSurfaceVariant)
                .padding(12.dp)
        ) {
            // Input field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CursorSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(CursorPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "Ask Cursor AI...",
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        color = TextTertiary
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(Modifier.width(8.dp))

            // Voice
            MiniVoiceButton(
                voiceState = voiceState,
                onClick = onVoiceInput
            )

            // Send
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                }
            ) {
                Icon(
                    Icons.Rounded.ArrowUpward,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) CursorPrimary else TextTertiary,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (inputText.isNotBlank()) CursorPrimary.copy(alpha = 0.2f)
                            else CursorSurfaceElevated
                        )
                        .padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val isAssistant = message.role == ChatRole.ASSISTANT

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (isAssistant) {
            // AI avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CursorPrimary.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = CursorPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) CursorPrimary.copy(alpha = 0.15f)
                    else CursorSurfaceElevated
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp
                ),
                color = TextPrimary
            )

            // Code blocks detection
            if (message.content.contains("```")) {
                // Simplified code block rendering
                Spacer(Modifier.height(4.dp))
            }

            // Streaming indicator
            if (message.isStreaming) {
                Spacer(Modifier.height(4.dp))
                StreamingDots()
            }
        }
    }
}

@Composable
fun StreamingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CursorPrimary.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun EmptyChatState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = CursorPrimary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Cursor AI Assistant",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Ask me anything about your code.\nUse voice input for hands-free control.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(24.dp))

        // Suggestion chips
        val suggestions = listOf(
            "Explain this code",
            "Find bugs",
            "Refactor function",
            "Write tests",
            "Add comments"
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            suggestions.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = CursorSurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                borderColor = BorderDefault
                            )
                        )
                    }
                }
            }
        }
    }
}
