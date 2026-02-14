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
    @SerializedName("preview_url") PREVIEW_URL,
    @SerializedName("deploy_command") DEPLOY_COMMAND,
    @SerializedName("deploy_output") DEPLOY_OUTPUT,
    @SerializedName("deploy_status") DEPLOY_STATUS
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
    EXPLORER, SEARCH, GIT, DEBUG, EXTENSIONS, AI_CHAT, PROJECT_PLAN, NONE
}

// ========== Project Planning System ==========

// Planning wizard step
enum class PlanningStep {
    WELCOME,           // Initial greeting, project type selection
    REQUIREMENTS,      // Gathering functional requirements
    TECH_STACK,        // Technology choices discussion
    ARCHITECTURE,      // Architecture & structure planning
    FEATURES,          // Feature breakdown and prioritization
    MILESTONES,        // Timeline and milestones
    REVIEW,            // Plan review and confirmation
    ACTIVE             // Plan is active, development in progress
}

// Project plan — the entire planning document
data class ProjectPlan(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val projectType: ProjectType = ProjectType.WEB_APP,
    val techStack: TechStack = TechStack(),
    val requirements: List<Requirement> = emptyList(),
    val features: List<Feature> = emptyList(),
    val phases: List<ProjectPhase> = emptyList(),
    val architecture: ArchitectureNotes = ArchitectureNotes(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
) {
    val completionPercent: Int
        get() {
            val total = features.size
            if (total == 0) return 0
            val done = features.count { it.status == FeatureStatus.DONE }
            return ((done.toFloat() / total) * 100).toInt()
        }
}

enum class ProjectType(val label: String, val emoji: String) {
    WEB_APP("Web Application", "🌐"),
    MOBILE_APP("Mobile App", "📱"),
    API_BACKEND("API / Backend", "⚡"),
    FULL_STACK("Full Stack", "🏗"),
    LIBRARY("Library / Package", "📦"),
    CLI_TOOL("CLI Tool", "💻"),
    DESKTOP_APP("Desktop App", "🖥"),
    GAME("Game", "🎮"),
    OTHER("Other", "✨")
}

data class TechStack(
    val language: String = "",
    val framework: String = "",
    val styling: String = "",
    val database: String = "",
    val testing: String = "",
    val deployment: String = "",
    val otherTools: List<String> = emptyList()
)

data class Requirement(
    val id: String = "",
    val text: String,
    val priority: Priority = Priority.MUST,
    val category: String = "functional"
)

enum class Priority(val label: String, val color: String) {
    MUST("Must Have", "red"),
    SHOULD("Should Have", "orange"),
    COULD("Could Have", "blue"),
    WONT("Won't Have", "gray")
}

data class Feature(
    val id: String = "",
    val title: String,
    val description: String = "",
    val phaseId: String = "",
    val priority: Priority = Priority.MUST,
    val status: FeatureStatus = FeatureStatus.TODO,
    val subtasks: List<Subtask> = emptyList(),
    val estimateHours: Float = 0f
)

enum class FeatureStatus(val label: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("In Review"),
    DONE("Done"),
    BLOCKED("Blocked")
}

data class Subtask(
    val id: String = "",
    val text: String,
    val isDone: Boolean = false
)

data class ProjectPhase(
    val id: String = "",
    val name: String,
    val description: String = "",
    val order: Int = 0,
    val featureIds: List<String> = emptyList(),
    val isComplete: Boolean = false
)

data class ArchitectureNotes(
    val folderStructure: String = "",
    val patterns: List<String> = emptyList(),
    val notes: String = ""
)

// Planning chat question from AI
data class PlanningQuestion(
    val id: String = "",
    val question: String,
    val category: String = "",     // "requirements", "tech", "features", etc.
    val options: List<String> = emptyList(),  // Suggested answers
    val isMultiSelect: Boolean = false,
    val answer: String = ""
)

// Bottom panel tab
enum class BottomPanelTab {
    PROBLEMS, OUTPUT, TERMINAL, AI_CHAT, DEBUG_CONSOLE, PREVIEW, BUILD, DEPLOY
}

// ========== Cloudflare / Wrangler Deployment ==========

enum class DeployState {
    IDLE, DEPLOYING, SUCCESS, FAILED
}

data class DeployTarget(
    val name: String,
    val command: String,
    val description: String = "",
    val icon: String = "cloud"  // cloud, workers, pages, r2, d1, kv
)

data class DeploymentInfo(
    val url: String = "",
    val workerName: String = "",
    val environment: String = "production",
    val timestamp: Long = 0,
    val version: String = "",
    val routes: List<String> = emptyList(),
    val size: String = ""
)

data class CloudflareProject(
    val name: String = "",
    val accountId: String = "",
    val hasWranglerToml: Boolean = false,
    val workerType: String = "",  // "worker", "pages", "durable-object"
    val bindings: List<String> = emptyList()  // KV, R2, D1, etc.
)

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
