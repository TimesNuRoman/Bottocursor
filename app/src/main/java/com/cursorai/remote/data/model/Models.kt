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
    @SerializedName("error") ERROR
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

// Quick action for voice commands
data class QuickAction(
    val icon: String,
    val label: String,
    val command: String,
    val description: String = ""
)
