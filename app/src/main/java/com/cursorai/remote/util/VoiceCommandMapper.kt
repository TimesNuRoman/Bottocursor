package com.cursorai.remote.util

/**
 * Maps voice commands in multiple languages to Cursor/VSCode actions.
 * Supports English and Russian out of the box.
 */
object VoiceCommandMapper {

    data class MappedCommand(
        val action: String,
        val confidence: Float,
        val parameters: Map<String, String> = emptyMap()
    )

    private val commandPatterns = listOf(
        // File operations
        CommandPattern(
            patterns = listOf("open file", "открой файл", "open", "откр"),
            action = "workbench.action.quickOpen"
        ),
        CommandPattern(
            patterns = listOf("save file", "save", "сохрани", "сохранить"),
            action = "workbench.action.files.save"
        ),
        CommandPattern(
            patterns = listOf("save all", "сохрани все", "сохранить всё"),
            action = "workbench.action.files.saveAll"
        ),
        CommandPattern(
            patterns = listOf("close tab", "close file", "закрой вкладку", "закрой файл"),
            action = "workbench.action.closeActiveEditor"
        ),
        CommandPattern(
            patterns = listOf("new file", "новый файл", "создай файл"),
            action = "workbench.action.files.newUntitledFile"
        ),

        // Navigation
        CommandPattern(
            patterns = listOf("go to line", "перейди на строку", "строка"),
            action = "workbench.action.gotoLine"
        ),
        CommandPattern(
            patterns = listOf("go to definition", "перейди к определению", "определение"),
            action = "editor.action.revealDefinition"
        ),
        CommandPattern(
            patterns = listOf("go back", "назад", "вернись"),
            action = "workbench.action.navigateBack"
        ),
        CommandPattern(
            patterns = listOf("go forward", "вперёд", "вперед"),
            action = "workbench.action.navigateForward"
        ),

        // Editing
        CommandPattern(
            patterns = listOf("undo", "отмени", "отмена", "отменить"),
            action = "undo"
        ),
        CommandPattern(
            patterns = listOf("redo", "повтори", "повторить", "вернуть"),
            action = "redo"
        ),
        CommandPattern(
            patterns = listOf("copy", "копируй", "копировать", "скопируй"),
            action = "editor.action.clipboardCopyAction"
        ),
        CommandPattern(
            patterns = listOf("cut", "вырежи", "вырезать"),
            action = "editor.action.clipboardCutAction"
        ),
        CommandPattern(
            patterns = listOf("paste", "вставь", "вставить"),
            action = "editor.action.clipboardPasteAction"
        ),
        CommandPattern(
            patterns = listOf("select all", "выдели все", "выделить всё"),
            action = "editor.action.selectAll"
        ),
        CommandPattern(
            patterns = listOf("find", "найди", "поиск", "искать"),
            action = "actions.find"
        ),
        CommandPattern(
            patterns = listOf("replace", "замени", "заменить"),
            action = "editor.action.startFindReplaceAction"
        ),
        CommandPattern(
            patterns = listOf("find in files", "найди в файлах", "глобальный поиск"),
            action = "workbench.action.findInFiles"
        ),
        CommandPattern(
            patterns = listOf("format", "форматируй", "форматировать", "отформатируй"),
            action = "editor.action.formatDocument"
        ),
        CommandPattern(
            patterns = listOf("comment", "комментарий", "закомментируй"),
            action = "editor.action.commentLine"
        ),
        CommandPattern(
            patterns = listOf("rename", "переименуй", "переименовать"),
            action = "editor.action.rename"
        ),

        // Terminal
        CommandPattern(
            patterns = listOf("open terminal", "terminal", "терминал", "открой терминал"),
            action = "workbench.action.terminal.toggleTerminal"
        ),
        CommandPattern(
            patterns = listOf("new terminal", "новый терминал"),
            action = "workbench.action.terminal.new"
        ),

        // Git
        CommandPattern(
            patterns = listOf("git commit", "коммит", "зафиксируй"),
            action = "git.commit"
        ),
        CommandPattern(
            patterns = listOf("git push", "пуш", "отправь"),
            action = "git.push"
        ),
        CommandPattern(
            patterns = listOf("git pull", "пул", "получи изменения"),
            action = "git.pull"
        ),
        CommandPattern(
            patterns = listOf("git status", "статус гита", "гит статус"),
            action = "git.status"
        ),

        // Debug
        CommandPattern(
            patterns = listOf("run", "запусти", "запустить", "выполни"),
            action = "workbench.action.debug.run"
        ),
        CommandPattern(
            patterns = listOf("debug", "дебаг", "отладка", "отладить"),
            action = "workbench.action.debug.start"
        ),
        CommandPattern(
            patterns = listOf("stop", "стоп", "останови", "остановить"),
            action = "workbench.action.debug.stop"
        ),

        // Cursor AI
        CommandPattern(
            patterns = listOf("compose", "composer", "композер"),
            action = "cursor.compose"
        ),
        CommandPattern(
            patterns = listOf("ai chat", "чат", "спроси"),
            action = "cursor.chat"
        ),
        CommandPattern(
            patterns = listOf("explain", "объясни", "объяснить"),
            action = "cursor.explain"
        ),
        CommandPattern(
            patterns = listOf("generate", "генерируй", "сгенерируй", "создай код"),
            action = "cursor.generate"
        ),
        CommandPattern(
            patterns = listOf("fix", "исправь", "починить", "пофикси"),
            action = "cursor.fix"
        ),

        // View
        CommandPattern(
            patterns = listOf("command palette", "палитра команд", "команды"),
            action = "workbench.action.showCommands"
        ),
        CommandPattern(
            patterns = listOf("sidebar", "боковая панель", "панель"),
            action = "workbench.action.toggleSidebarVisibility"
        ),
        CommandPattern(
            patterns = listOf("zoom in", "увеличь", "приблизь"),
            action = "workbench.action.zoomIn"
        ),
        CommandPattern(
            patterns = listOf("zoom out", "уменьши", "отдали"),
            action = "workbench.action.zoomOut"
        ),
    )

    fun mapCommand(text: String): MappedCommand? {
        val lower = text.lowercase().trim()

        // Try exact and fuzzy matching
        for (pattern in commandPatterns) {
            for (p in pattern.patterns) {
                if (lower.contains(p)) {
                    val matchLength = p.length.toFloat()
                    val textLength = lower.length.toFloat()
                    val confidence = (matchLength / textLength).coerceIn(0.3f, 1f)

                    return MappedCommand(
                        action = pattern.action,
                        confidence = confidence
                    )
                }
            }
        }

        return null
    }

    private data class CommandPattern(
        val patterns: List<String>,
        val action: String
    )
}
