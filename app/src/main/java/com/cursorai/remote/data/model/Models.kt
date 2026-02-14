package com.cursorai.remote.data.model

import com.google.gson.annotations.SerializedName

// Connection state
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

// Message types for WebSocket communication
enum class MessageType {
    @SerializedName("command") COMMAND,
    @SerializedName("voice_command") VOICE_COMMAND,
    @SerializedName("ai_prompt") AI_PROMPT,
    @SerializedName("file_request") FILE_REQUEST,
    @SerializedName("terminal_input") TERMINAL_INPUT,
    @SerializedName("editor_action") EDITOR_ACTION,
    @SerializedName("cursor_move") CURSOR_MOVE,
    @SerializedName("response") RESPONSE,
    @SerializedName("file_tree") FILE_TREE,
    @SerializedName("file_content") FILE_CONTENT,
    @SerializedName("terminal_output") TERMINAL_OUTPUT,
    @SerializedName("ai_response") AI_RESPONSE,
    @SerializedName("status") STATUS,
    @SerializedName("error") ERROR,
    @SerializedName("build_command") BUILD_COMMAND,
    @SerializedName("build_output") BUILD_OUTPUT,
    @SerializedName("build_status") BUILD_STATUS,
    @SerializedName("dev_server") DEV_SERVER,
    @SerializedName("dev_server_status") DEV_SERVER_STATUS,
    @SerializedName("preview_url") PREVIEW_URL
}

// WebSocket message envelope
data class WsMessage(
    val type: MessageType,
    val payload: String = "",
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// File tree node
data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val extension: String = ""
)

// AI Chat message
data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

enum class ChatRole {
    USER, ASSISTANT, SYSTEM
}

// Terminal line
data class TerminalLine(
    val text: String,
    val isCommand: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

// Connection config
data class ConnectionConfig(
    val host: String = "192.168.1.100",
    val port: Int = 9090,
    val useTls: Boolean = false,
    val authToken: String = ""
) {
    val wsUrl: String
        get() = "${if (useTls) "wss" else "ws"}://$host:$port/ws"
}

// Voice command result
data class VoiceCommand(
    val rawText: String,
    val confidence: Float,
    val interpretedAction: String = "",
    val parameters: Map<String, String> = emptyMap()
)

// Editor state
data class EditorState(
    val filePath: String = "",
    val fileName: String = "",
    val content: String = "",
    val language: String = "",
    val cursorLine: Int = 0,
    val cursorColumn: Int = 0,
    val isModified: Boolean = false
)

// Editor tab (open file)
data class EditorTab(
    val filePath: String,
    val fileName: String,
    val language: String = "",
    val isModified: Boolean = false,
    val isPinned: Boolean = false
)

// Sidebar panel type (Activity Bar sections)
enum class SidebarPanel {
    EXPLORER, SEARCH, GIT, DEBUG, EXTENSIONS, AI_CHAT, NONE
}

// Bottom panel tab
enum class BottomPanelTab {
    PROBLEMS, OUTPUT, TERMINAL, AI_CHAT, DEBUG_CONSOLE, PREVIEW, BUILD
}

// Build state
enum class BuildState {
    IDLE, BUILDING, SUCCESS, FAILED
}

// Build task configuration
data class BuildTask(
    val name: String,
    val command: String,
    val description: String = "",
    val isDevServer: Boolean = false
)

// Dev server state
data class DevServerState(
    val isRunning: Boolean = false,
    val url: String = "",
    val port: Int = 0,
    val framework: String = "",
    val pid: Int = 0
)

// Preview device mode
enum class PreviewDevice(val label: String, val width: Int, val height: Int) {
    RESPONSIVE("Responsive", 0, 0),
    IPHONE_SE("iPhone SE", 375, 667),
    IPHONE_14("iPhone 14", 390, 844),
    IPHONE_14_PRO_MAX("iPhone 14 Pro Max", 430, 932),
    PIXEL_7("Pixel 7", 412, 915),
    IPAD("iPad", 810, 1080),
    IPAD_PRO("iPad Pro 12.9", 1024, 1366),
    DESKTOP_HD("Desktop HD", 1920, 1080),
    DESKTOP_4K("Desktop 4K", 3840, 2160)
}

// Console log entry from WebView
data class ConsoleLogEntry(
    val level: String, // log, warn, error, info
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Problem/diagnostic entry
data class DiagnosticEntry(
    val message: String,
    val file: String,
    val line: Int,
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR
)

enum class DiagnosticSeverity {
    ERROR, WARNING, INFO, HINT
}

// Quick action for voice commands
data class QuickAction(
    val icon: String,
    val label: String,
    val command: String,
    val description: String = ""
)
