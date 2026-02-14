package com.cursorai.remote.ui.screens

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
import com.cursorai.remote.data.model.TerminalLine
import com.cursorai.remote.service.VoiceState
import com.cursorai.remote.ui.components.MiniVoiceButton
import com.cursorai.remote.ui.theme.*

@Composable
fun TerminalScreen(
    terminalLines: List<TerminalLine>,
    voiceState: VoiceState,
    onSendCommand: (String) -> Unit,
    onVoiceInput: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom
    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Terminal header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(CursorSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Terminal,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "TERMINAL",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { /* clear terminal */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { /* new terminal */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "New Terminal",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Terminal output
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(terminalLines) { line ->
                TerminalLineItem(line = line)
            }
        }

        // Input bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(CursorSurfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Prompt
            Text(
                text = "$ ",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = TerminalPrompt,
                    fontWeight = FontWeight.Bold
                )
            )

            // Text input
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = TerminalCommand
                ),
                cursorBrush = SolidColor(CursorPrimary),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type a command...",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = TextTertiary
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Voice button
            MiniVoiceButton(
                voiceState = voiceState,
                onClick = onVoiceInput
            )

            // Send button
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendCommand(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank()
            ) {
                Icon(
                    Icons.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) CursorPrimary else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TerminalLineItem(line: TerminalLine) {
    Text(
        text = line.text,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            color = when {
                line.isCommand -> TerminalCommand
                line.text.startsWith("Error") || line.text.startsWith("error") -> TerminalError
                line.text.contains("➜") -> TerminalPrompt
                else -> TerminalText
            },
            fontWeight = if (line.isCommand) FontWeight.Medium else FontWeight.Normal
        ),
        modifier = Modifier.padding(vertical = 1.dp)
    )
}
