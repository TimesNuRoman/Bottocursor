package com.cursorai.remote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.ConnectionConfig
import com.cursorai.remote.data.model.ConnectionState
import com.cursorai.remote.ui.theme.*

@Composable
fun SettingsScreen(
    connectionConfig: ConnectionConfig,
    connectionState: ConnectionState,
    voiceLanguage: String,
    autoConnect: Boolean,
    hapticFeedback: Boolean,
    fontSize: Int,
    onSaveConfig: (ConnectionConfig) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSaveVoiceLanguage: (String) -> Unit,
    onSaveAutoConnect: (Boolean) -> Unit,
    onSaveHapticFeedback: (Boolean) -> Unit,
    onSaveFontSize: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var host by remember(connectionConfig.host) { mutableStateOf(connectionConfig.host) }
    var port by remember(connectionConfig.port) { mutableStateOf(connectionConfig.port.toString()) }
    var token by remember(connectionConfig.authToken) { mutableStateOf(connectionConfig.authToken) }
    var useTls by remember(connectionConfig.useTls) { mutableStateOf(connectionConfig.useTls) }
    var selectedLanguage by remember(voiceLanguage) { mutableStateOf(voiceLanguage) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CursorSurface)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CursorSurfaceVariant)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Configure your Cursor AI Remote connection",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(16.dp))

        // Connection Section
        SettingsSection(
            title = "Connection",
            icon = Icons.Rounded.Wifi
        ) {
            SettingsTextField(
                label = "Host Address",
                value = host,
                onValueChange = { host = it },
                placeholder = "192.168.1.100",
                keyboardType = KeyboardType.Uri
            )

            Spacer(Modifier.height(12.dp))

            SettingsTextField(
                label = "Port",
                value = port,
                onValueChange = { port = it },
                placeholder = "9090",
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(12.dp))

            SettingsTextField(
                label = "Auth Token (optional)",
                value = token,
                onValueChange = { token = it },
                placeholder = "Enter token...",
                keyboardType = KeyboardType.Password
            )

            Spacer(Modifier.height(12.dp))

            SettingsToggle(
                label = "Use TLS (WSS)",
                description = "Enable secure WebSocket connection",
                checked = useTls,
                onCheckedChange = { useTls = it }
            )

            Spacer(Modifier.height(12.dp))

            SettingsToggle(
                label = "Auto Connect",
                description = "Connect automatically on app start",
                checked = autoConnect,
                onCheckedChange = onSaveAutoConnect
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val config = ConnectionConfig(
                            host = host,
                            port = port.toIntOrNull() ?: 9090,
                            useTls = useTls,
                            authToken = token
                        )
                        onSaveConfig(config)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CursorSurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save")
                }

                Button(
                    onClick = {
                        if (connectionState == ConnectionState.CONNECTED) {
                            onDisconnect()
                        } else {
                            val config = ConnectionConfig(
                                host = host,
                                port = port.toIntOrNull() ?: 9090,
                                useTls = useTls,
                                authToken = token
                            )
                            onSaveConfig(config)
                            onConnect()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (connectionState == ConnectionState.CONNECTED)
                            StatusError else CursorPrimary,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        if (connectionState == ConnectionState.CONNECTED) Icons.Rounded.LinkOff
                        else Icons.Rounded.Link,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (connectionState == ConnectionState.CONNECTED) "Disconnect"
                        else "Connect"
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Voice Section
        SettingsSection(
            title = "Voice Input",
            icon = Icons.Rounded.Mic
        ) {
            Text(
                text = "Language",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))

            val languages = listOf(
                "en-US" to "English (US)",
                "ru-RU" to "Русский",
                "de-DE" to "Deutsch",
                "fr-FR" to "Français",
                "es-ES" to "Español",
                "ja-JP" to "日本語",
                "zh-CN" to "中文",
                "ko-KR" to "한국어"
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                languages.forEach { (code, name) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedLanguage == code) CursorPrimary.copy(alpha = 0.15f)
                                else CursorSurfaceElevated
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = selectedLanguage == code,
                            onClick = {
                                selectedLanguage = code
                                onSaveVoiceLanguage(code)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CursorPrimary,
                                unselectedColor = TextTertiary
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedLanguage == code) TextPrimary else TextSecondary
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Appearance Section
        SettingsSection(
            title = "Appearance",
            icon = Icons.Rounded.Palette
        ) {
            SettingsToggle(
                label = "Haptic Feedback",
                description = "Vibration on voice input and actions",
                checked = hapticFeedback,
                onCheckedChange = onSaveHapticFeedback
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Editor Font Size: ${fontSize}sp",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            Slider(
                value = fontSize.toFloat(),
                onValueChange = { onSaveFontSize(it.toInt()) },
                valueRange = 10f..24f,
                steps = 13,
                colors = SliderDefaults.colors(
                    thumbColor = CursorPrimary,
                    activeTrackColor = CursorPrimary,
                    inactiveTrackColor = CursorSurfaceHigh
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // About section
        SettingsSection(
            title = "About",
            icon = Icons.Rounded.Info
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Version", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("1.0.0", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Protocol", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("WebSocket v1", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CursorPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CursorSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = TextPrimary
            ),
            cursorBrush = SolidColor(CursorPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CursorSurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(fontSize = 15.sp, color = TextTertiary)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CursorPrimary,
                checkedTrackColor = CursorPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = CursorSurfaceElevated
            )
        )
    }
}
