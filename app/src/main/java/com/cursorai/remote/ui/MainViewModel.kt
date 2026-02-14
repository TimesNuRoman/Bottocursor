package com.cursorai.remote.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cursorai.remote.data.model.*
import com.cursorai.remote.data.network.WebSocketManager
import com.cursorai.remote.data.repository.SettingsRepository
import com.cursorai.remote.service.VoiceInputService
import com.cursorai.remote.service.VoiceState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    val webSocketManager = WebSocketManager()
    val voiceService = VoiceInputService(application)
    private val gson = Gson()

    // Connection
    val connectionState = webSocketManager.connectionState
    val latency = webSocketManager.latency

    // Settings
    val connectionConfig = settingsRepository.connectionConfig
        .stateIn(viewModelScope, SharingStarted.Lazily, ConnectionConfig())
    val voiceLanguage = settingsRepository.voiceLanguage
        .stateIn(viewModelScope, SharingStarted.Lazily, "en-US")
    val autoConnect = settingsRepository.autoConnect
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    val hapticFeedback = settingsRepository.hapticFeedback
        .stateIn(viewModelScope, SharingStarted.Lazily, true)
    val fontSize = settingsRepository.fontSize
        .stateIn(viewModelScope, SharingStarted.Lazily, 14)

    // Voice
    val voiceState = voiceService.voiceState
    val partialText = voiceService.partialText
    val amplitude = voiceService.amplitude

    // UI State
    private val _currentTab = MutableStateFlow(AppTab.EDITOR)
    val currentTab: StateFlow<AppTab> = _currentTab

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines

    private val _fileTree = MutableStateFlow<List<FileNode>>(emptyList())
    val fileTree: StateFlow<List<FileNode>> = _fileTree

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState

    private val _commandHistory = MutableStateFlow<List<String>>(emptyList())
    val commandHistory: StateFlow<List<String>> = _commandHistory

    private val _showVoiceOverlay = MutableStateFlow(false)
    val showVoiceOverlay: StateFlow<Boolean> = _showVoiceOverlay

    // Tablet IDE state
    private val _sidebarPanel = MutableStateFlow(SidebarPanel.EXPLORER)
    val sidebarPanel: StateFlow<SidebarPanel> = _sidebarPanel

    private val _sidebarVisible = MutableStateFlow(true)
    val sidebarVisible: StateFlow<Boolean> = _sidebarVisible

    private val _bottomPanelTab = MutableStateFlow(BottomPanelTab.TERMINAL)
    val bottomPanelTab: StateFlow<BottomPanelTab> = _bottomPanelTab

    private val _bottomPanelVisible = MutableStateFlow(true)
    val bottomPanelVisible: StateFlow<Boolean> = _bottomPanelVisible

    private val _openTabs = MutableStateFlow<List<EditorTab>>(emptyList())
    val openTabs: StateFlow<List<EditorTab>> = _openTabs

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex

    private val _diagnostics = MutableStateFlow<List<DiagnosticEntry>>(emptyList())
    val diagnostics: StateFlow<List<DiagnosticEntry>> = _diagnostics

    private val _gitBranch = MutableStateFlow("main")
    val gitBranch: StateFlow<String> = _gitBranch

    private val _outputLines = MutableStateFlow<List<String>>(emptyList())
    val outputLines: StateFlow<List<String>> = _outputLines

    // Preview & Build state
    private val _previewUrl = MutableStateFlow("")
    val previewUrl: StateFlow<String> = _previewUrl

    private val _isDevServerRunning = MutableStateFlow(false)
    val isDevServerRunning: StateFlow<Boolean> = _isDevServerRunning

    private val _consoleLogs = MutableStateFlow<List<ConsoleLogEntry>>(emptyList())
    val consoleLogs: StateFlow<List<ConsoleLogEntry>> = _consoleLogs

    private val _selectedDevice = MutableStateFlow(PreviewDevice.RESPONSIVE)
    val selectedDevice: StateFlow<PreviewDevice> = _selectedDevice

    private val _buildState = MutableStateFlow(BuildState.IDLE)
    val buildState: StateFlow<BuildState> = _buildState

    private val _buildOutput = MutableStateFlow<List<String>>(emptyList())
    val buildOutput: StateFlow<List<String>> = _buildOutput

    val buildTasks = listOf(
        BuildTask("dev", "npm run dev", "Start development server", isDevServer = true),
        BuildTask("build", "npm run build", "Production build"),
        BuildTask("test", "npm test", "Run tests"),
        BuildTask("lint", "npm run lint", "Run linter"),
        BuildTask("preview", "npm run preview", "Preview production build", isDevServer = true),
        BuildTask("install", "npm install", "Install dependencies"),
        BuildTask("clean", "rm -rf node_modules/.cache dist", "Clean build cache"),
        BuildTask("typecheck", "npx tsc --noEmit", "TypeScript type check"),
    )

    // Quick actions for voice commands
    val quickActions = listOf(
        QuickAction("terminal", "Open Terminal", "workbench.action.terminal.toggleTerminal", "Toggle integrated terminal"),
        QuickAction("search", "Find in Files", "workbench.action.findInFiles", "Search across project"),
        QuickAction("git", "Git Status", "git.status", "Show git status"),
        QuickAction("run", "Run Code", "workbench.action.debug.run", "Run current file"),
        QuickAction("format", "Format Code", "editor.action.formatDocument", "Format current document"),
        QuickAction("save", "Save All", "workbench.action.files.saveAll", "Save all open files"),
        QuickAction("ai", "AI Compose", "cursor.compose", "Open Cursor AI Composer"),
        QuickAction("chat", "AI Chat", "cursor.chat", "Open Cursor AI Chat"),
        QuickAction("undo", "Undo", "undo", "Undo last action"),
        QuickAction("redo", "Redo", "redo", "Redo last action"),
        QuickAction("palette", "Command Palette", "workbench.action.showCommands", "Open command palette"),
        QuickAction("close", "Close Tab", "workbench.action.closeActiveEditor", "Close current tab"),
    )

    init {
        // Listen for voice language changes
        viewModelScope.launch {
            voiceLanguage.collect { lang ->
                voiceService.setLanguage(lang)
            }
        }

        // Listen for incoming messages
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                handleMessage(message)
            }
        }

        // Listen for recognized voice text
        viewModelScope.launch {
            voiceService.recognizedText.collect { text ->
                if (text.isNotBlank()) {
                    processVoiceCommand(text)
                }
            }
        }

        // Add demo data for preview
        addDemoData()
    }

    private fun addDemoData() {
        _fileTree.value = listOf(
            FileNode("src", "src", true, listOf(
                FileNode("main.ts", "src/main.ts", false, extension = "ts"),
                FileNode("app.tsx", "src/app.tsx", false, extension = "tsx"),
                FileNode("index.css", "src/index.css", false, extension = "css"),
                FileNode("components", "src/components", true, listOf(
                    FileNode("Header.tsx", "src/components/Header.tsx", false, extension = "tsx"),
                    FileNode("Sidebar.tsx", "src/components/Sidebar.tsx", false, extension = "tsx"),
                    FileNode("Footer.tsx", "src/components/Footer.tsx", false, extension = "tsx"),
                )),
                FileNode("hooks", "src/hooks", true, listOf(
                    FileNode("useAuth.ts", "src/hooks/useAuth.ts", false, extension = "ts"),
                    FileNode("useTheme.ts", "src/hooks/useTheme.ts", false, extension = "ts"),
                )),
                FileNode("utils", "src/utils", true, listOf(
                    FileNode("api.ts", "src/utils/api.ts", false, extension = "ts"),
                    FileNode("helpers.ts", "src/utils/helpers.ts", false, extension = "ts"),
                )),
                FileNode("types", "src/types", true, listOf(
                    FileNode("index.d.ts", "src/types/index.d.ts", false, extension = "ts"),
                )),
            )),
            FileNode("public", "public", true, listOf(
                FileNode("favicon.ico", "public/favicon.ico", false, extension = "ico"),
                FileNode("index.html", "public/index.html", false, extension = "html"),
            )),
            FileNode(".gitignore", ".gitignore", false, extension = "gitignore"),
            FileNode("package.json", "package.json", false, extension = "json"),
            FileNode("tsconfig.json", "tsconfig.json", false, extension = "json"),
            FileNode("vite.config.ts", "vite.config.ts", false, extension = "ts"),
            FileNode("README.md", "README.md", false, extension = "md"),
        )

        _editorState.value = EditorState(
            filePath = "src/app.tsx",
            fileName = "app.tsx",
            content = """import React from 'react';
import { Header } from './components/Header';
import { Sidebar } from './components/Sidebar';

interface AppProps {
  title: string;
  theme: 'light' | 'dark';
}

export const App: React.FC<AppProps> = ({ title, theme }) => {
  const [count, setCount] = React.useState(0);

  React.useEffect(() => {
    document.title = title;
  }, [title]);

  return (
    <div className={`app app--${'$'}{theme}`}>
      <Header title={title} />
      <main className="app__content">
        <Sidebar />
        <section className="app__main">
          <h1>{title}</h1>
          <p>Count: {count}</p>
          <button onClick={() => setCount(c => c + 1)}>
            Increment
          </button>
        </section>
      </main>
    </div>
  );
};""",
            language = "typescriptreact",
            cursorLine = 12,
            cursorColumn = 4
        )

        // Open editor tabs
        _openTabs.value = listOf(
            EditorTab("src/app.tsx", "app.tsx", "typescriptreact", isModified = true),
            EditorTab("src/main.ts", "main.ts", "typescript"),
            EditorTab("src/components/Header.tsx", "Header.tsx", "typescriptreact"),
            EditorTab("package.json", "package.json", "json"),
        )
        _activeTabIndex.value = 0

        // Demo diagnostics
        _diagnostics.value = listOf(
            DiagnosticEntry("Property 'theme' is missing in type '{}'", "src/app.tsx", 19, DiagnosticSeverity.ERROR),
            DiagnosticEntry("'count' is declared but never read", "src/app.tsx", 11, DiagnosticSeverity.WARNING),
            DiagnosticEntry("Unexpected any. Specify a different type", "src/utils/api.ts", 5, DiagnosticSeverity.WARNING),
        )

        _terminalLines.value = listOf(
            TerminalLine("$ npm run dev", isCommand = true),
            TerminalLine(""),
            TerminalLine("  VITE v5.0.12  ready in 245 ms"),
            TerminalLine(""),
            TerminalLine("  ➜  Local:   http://localhost:5173/"),
            TerminalLine("  ➜  Network: http://192.168.1.100:5173/"),
            TerminalLine("  ➜  press h + enter to show help"),
        )

        _outputLines.value = listOf(
            "[Info  - 12:00:01] TypeScript Server started",
            "[Info  - 12:00:02] Loading project: /workspace/tsconfig.json",
            "[Info  - 12:00:03] Files: 47, Symbols: 12840",
        )
    }

    private fun handleMessage(message: WsMessage) {
        when (message.type) {
            MessageType.AI_RESPONSE -> {
                val existing = _chatMessages.value.toMutableList()
                val streamingIdx = existing.indexOfLast { it.isStreaming }
                if (streamingIdx >= 0) {
                    existing[streamingIdx] = existing[streamingIdx].copy(
                        content = existing[streamingIdx].content + message.payload,
                    )
                } else {
                    existing.add(
                        ChatMessage(
                            id = message.id,
                            role = ChatRole.ASSISTANT,
                            content = message.payload,
                            isStreaming = true
                        )
                    )
                }
                _chatMessages.value = existing
            }

            MessageType.FILE_TREE -> {
                try {
                    val type = object : TypeToken<List<FileNode>>() {}.type
                    val nodes: List<FileNode> = gson.fromJson(message.payload, type)
                    _fileTree.value = nodes
                } catch (_: Exception) {}
            }

            MessageType.FILE_CONTENT -> {
                try {
                    val editor: EditorState = gson.fromJson(message.payload, EditorState::class.java)
                    _editorState.value = editor
                } catch (_: Exception) {}
            }

            MessageType.TERMINAL_OUTPUT -> {
                val lines = _terminalLines.value.toMutableList()
                lines.add(TerminalLine(message.payload))
                _terminalLines.value = lines
            }

            MessageType.STATUS -> {
                // Handle status/pong
            }

            MessageType.BUILD_OUTPUT -> {
                val lines = _buildOutput.value.toMutableList()
                lines.add(message.payload)
                _buildOutput.value = lines
            }

            MessageType.BUILD_STATUS -> {
                when (message.payload) {
                    "building" -> _buildState.value = BuildState.BUILDING
                    "success" -> _buildState.value = BuildState.SUCCESS
                    "failed" -> _buildState.value = BuildState.FAILED
                    "idle" -> _buildState.value = BuildState.IDLE
                }
            }

            MessageType.DEV_SERVER_STATUS -> {
                try {
                    val state = gson.fromJson(message.payload, DevServerState::class.java)
                    _isDevServerRunning.value = state.isRunning
                    if (state.url.isNotBlank()) {
                        _previewUrl.value = state.url
                    }
                } catch (_: Exception) {
                    _isDevServerRunning.value = message.payload == "running"
                }
            }

            MessageType.PREVIEW_URL -> {
                _previewUrl.value = message.payload
                _isDevServerRunning.value = true
            }

            MessageType.ERROR -> {
                val lines = _terminalLines.value.toMutableList()
                lines.add(TerminalLine("Error: ${message.payload}"))
                _terminalLines.value = lines
            }

            else -> {}
        }
    }

    fun processVoiceCommand(text: String) {
        _showVoiceOverlay.value = false

        // Add to command history
        val history = _commandHistory.value.toMutableList()
        history.add(0, text)
        if (history.size > 50) history.removeAt(history.lastIndex)
        _commandHistory.value = history

        // Map common voice commands to Cursor actions
        val lowerText = text.lowercase()
        val command = when {
            // Navigation
            lowerText.contains("open terminal") || lowerText.contains("открой терминал") ->
                "workbench.action.terminal.toggleTerminal"
            lowerText.contains("open file") || lowerText.contains("открой файл") ->
                "workbench.action.quickOpen"
            lowerText.contains("close tab") || lowerText.contains("закрой вкладку") ->
                "workbench.action.closeActiveEditor"
            lowerText.contains("save") || lowerText.contains("сохрани") ->
                "workbench.action.files.save"
            lowerText.contains("save all") || lowerText.contains("сохрани все") ->
                "workbench.action.files.saveAll"

            // Editing
            lowerText.contains("undo") || lowerText.contains("отмени") ->
                "undo"
            lowerText.contains("redo") || lowerText.contains("повтори") ->
                "redo"
            lowerText.contains("format") || lowerText.contains("форматируй") ->
                "editor.action.formatDocument"
            lowerText.contains("find") || lowerText.contains("найди") || lowerText.contains("поиск") ->
                "workbench.action.findInFiles"
            lowerText.contains("replace") || lowerText.contains("замени") ->
                "editor.action.startFindReplaceAction"
            lowerText.contains("comment") || lowerText.contains("комментарий") ->
                "editor.action.commentLine"
            lowerText.contains("select all") || lowerText.contains("выдели все") ->
                "editor.action.selectAll"
            lowerText.contains("copy") || lowerText.contains("копируй") ->
                "editor.action.clipboardCopyAction"
            lowerText.contains("cut") || lowerText.contains("вырежи") ->
                "editor.action.clipboardCutAction"
            lowerText.contains("paste") || lowerText.contains("вставь") ->
                "editor.action.clipboardPasteAction"

            // Git
            lowerText.contains("git commit") || lowerText.contains("коммит") ->
                "git.commit"
            lowerText.contains("git push") || lowerText.contains("пуш") ->
                "git.push"
            lowerText.contains("git pull") || lowerText.contains("пул") ->
                "git.pull"
            lowerText.contains("git status") || lowerText.contains("статус") ->
                "git.status"

            // Run
            lowerText.contains("run") || lowerText.contains("запусти") ->
                "workbench.action.debug.run"
            lowerText.contains("debug") || lowerText.contains("дебаг") || lowerText.contains("отладка") ->
                "workbench.action.debug.start"
            lowerText.contains("stop") || lowerText.contains("стоп") || lowerText.contains("останови") ->
                "workbench.action.debug.stop"

            // AI specific
            lowerText.contains("compose") || lowerText.contains("композер") || lowerText.contains("composer") ->
                "cursor.compose"
            lowerText.contains("ai chat") || lowerText.contains("чат") ->
                "cursor.chat"
            lowerText.contains("explain") || lowerText.contains("объясни") ->
                "cursor.explain"
            lowerText.contains("generate") || lowerText.contains("генерируй") || lowerText.contains("сгенерируй") ->
                "cursor.generate"

            // Build & Preview
            lowerText.contains("preview") || lowerText.contains("превью") || lowerText.contains("предпросмотр") -> {
                openPreview()
                null
            }
            lowerText.contains("build") || lowerText.contains("билд") || lowerText.contains("собери") ||
                    lowerText.contains("сборка") -> {
                openBuild()
                "workbench.action.tasks.build"
            }
            lowerText.contains("start server") || lowerText.contains("dev server") ||
                    lowerText.contains("запусти сервер") || lowerText.contains("дев сервер") -> {
                startDevServer()
                null
            }
            lowerText.contains("stop server") || lowerText.contains("останови сервер") -> {
                stopDevServer()
                null
            }
            lowerText.contains("test") || lowerText.contains("тест") -> {
                runBuildTask(buildTasks.first { it.name == "test" })
                null
            }
            lowerText.contains("lint") || lowerText.contains("линт") -> {
                runBuildTask(buildTasks.first { it.name == "lint" })
                null
            }

            // Command palette
            lowerText.contains("command") || lowerText.contains("palette") || lowerText.contains("команд") ->
                "workbench.action.showCommands"

            // Default - send as AI prompt
            else -> null
        }

        if (command != null) {
            webSocketManager.sendCommand(command)
            addChatMessage(ChatRole.USER, "Voice: $text → $command")
        } else {
            // If no recognized command, send as AI prompt
            webSocketManager.sendAIPrompt(text)
            addChatMessage(ChatRole.USER, text)
        }
    }

    fun addChatMessage(role: ChatRole, content: String) {
        val messages = _chatMessages.value.toMutableList()
        // Finalize any streaming messages
        messages.forEachIndexed { index, msg ->
            if (msg.isStreaming) {
                messages[index] = msg.copy(isStreaming = false)
            }
        }
        messages.add(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                role = role,
                content = content
            )
        )
        _chatMessages.value = messages
    }

    fun connect() {
        viewModelScope.launch {
            val config = connectionConfig.value
            webSocketManager.connect(config)
        }
    }

    fun disconnect() {
        webSocketManager.disconnect()
    }

    fun saveSettings(config: ConnectionConfig) {
        viewModelScope.launch {
            settingsRepository.saveConnectionConfig(config)
        }
    }

    fun saveVoiceLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.saveVoiceLanguage(language)
        }
    }

    fun saveAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveAutoConnect(enabled)
        }
    }

    fun saveHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveHapticFeedback(enabled)
        }
    }

    fun saveFontSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.saveFontSize(size)
        }
    }

    fun setCurrentTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleVoiceOverlay() {
        _showVoiceOverlay.value = !_showVoiceOverlay.value
    }

    fun startVoiceInput() {
        _showVoiceOverlay.value = true
        voiceService.startListening()
    }

    fun stopVoiceInput() {
        voiceService.stopListening()
    }

    fun sendTerminalCommand(command: String) {
        val lines = _terminalLines.value.toMutableList()
        lines.add(TerminalLine("$ $command", isCommand = true))
        _terminalLines.value = lines
        webSocketManager.sendTerminalInput(command)
    }

    fun sendAIMessage(message: String) {
        addChatMessage(ChatRole.USER, message)
        webSocketManager.sendAIPrompt(message)
    }

    fun requestFile(path: String) {
        webSocketManager.sendCommand("vscode.open:$path")
    }

    fun executeQuickAction(action: QuickAction) {
        webSocketManager.sendCommand(action.command)
    }

    // Tablet IDE panel management
    fun setSidebarPanel(panel: SidebarPanel) {
        if (_sidebarPanel.value == panel && _sidebarVisible.value) {
            _sidebarVisible.value = false
        } else {
            _sidebarPanel.value = panel
            _sidebarVisible.value = true
        }
    }

    fun toggleSidebar() {
        _sidebarVisible.value = !_sidebarVisible.value
    }

    fun setBottomPanelTab(tab: BottomPanelTab) {
        if (_bottomPanelTab.value == tab && _bottomPanelVisible.value) {
            _bottomPanelVisible.value = false
        } else {
            _bottomPanelTab.value = tab
            _bottomPanelVisible.value = true
        }
    }

    fun toggleBottomPanel() {
        _bottomPanelVisible.value = !_bottomPanelVisible.value
    }

    fun setActiveTab(index: Int) {
        if (index in _openTabs.value.indices) {
            _activeTabIndex.value = index
        }
    }

    fun closeTab(index: Int) {
        val tabs = _openTabs.value.toMutableList()
        if (tabs.size > 1 && index in tabs.indices) {
            tabs.removeAt(index)
            _openTabs.value = tabs
            if (_activeTabIndex.value >= tabs.size) {
                _activeTabIndex.value = tabs.size - 1
            }
        }
    }

    // ========== Build & Preview ==========

    fun runBuildTask(task: BuildTask) {
        _buildOutput.value = listOf("$ ${task.command}")
        _buildState.value = BuildState.BUILDING

        if (task.isDevServer) {
            webSocketManager.send(WsMessage(
                type = MessageType.DEV_SERVER,
                payload = task.command,
                id = java.util.UUID.randomUUID().toString()
            ))
        } else {
            webSocketManager.send(WsMessage(
                type = MessageType.BUILD_COMMAND,
                payload = task.command,
                id = java.util.UUID.randomUUID().toString()
            ))
        }
    }

    fun stopBuild() {
        webSocketManager.send(WsMessage(
            type = MessageType.BUILD_COMMAND,
            payload = "STOP",
            id = java.util.UUID.randomUUID().toString()
        ))
        _buildState.value = BuildState.IDLE
    }

    fun startDevServer() {
        val devTask = buildTasks.first { it.isDevServer }
        runBuildTask(devTask)
        // Switch to preview tab
        _bottomPanelTab.value = BottomPanelTab.PREVIEW
        _bottomPanelVisible.value = true
    }

    fun stopDevServer() {
        webSocketManager.send(WsMessage(
            type = MessageType.DEV_SERVER,
            payload = "STOP",
            id = java.util.UUID.randomUUID().toString()
        ))
        _isDevServerRunning.value = false
        _previewUrl.value = ""
    }

    fun setPreviewUrl(url: String) {
        _previewUrl.value = url
    }

    fun setPreviewDevice(device: PreviewDevice) {
        _selectedDevice.value = device
    }

    fun clearConsoleLogs() {
        _consoleLogs.value = emptyList()
    }

    fun clearBuildOutput() {
        _buildOutput.value = emptyList()
        _buildState.value = BuildState.IDLE
    }

    fun openPreview() {
        _bottomPanelTab.value = BottomPanelTab.PREVIEW
        _bottomPanelVisible.value = true
    }

    fun openBuild() {
        _bottomPanelTab.value = BottomPanelTab.BUILD
        _bottomPanelVisible.value = true
    }

    override fun onCleared() {
        super.onCleared()
        voiceService.destroy()
        webSocketManager.disconnect()
    }
}

enum class AppTab(val label: String, val icon: String) {
    EDITOR("Editor", "code"),
    FILES("Files", "folder"),
    TERMINAL("Terminal", "terminal"),
    AI_CHAT("AI Chat", "smart_toy"),
    SETTINGS("Settings", "settings")
}
