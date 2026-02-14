package com.cursorai.remote.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.EditorState
import com.cursorai.remote.ui.theme.*

@Composable
fun EditorScreen(
    editorState: EditorState,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Tab bar
        EditorTabBar(
            fileName = editorState.fileName,
            filePath = editorState.filePath,
            language = editorState.language,
            isModified = editorState.isModified
        )

        // Toolbar
        EditorToolbar(onAction = onAction)

        // Editor content
        Row(modifier = Modifier.weight(1f)) {
            // Line numbers gutter
            LineNumberGutter(
                content = editorState.content,
                currentLine = editorState.cursorLine,
                modifier = Modifier.fillMaxHeight()
            )

            // Code content
            CodeContent(
                content = editorState.content,
                language = editorState.language,
                currentLine = editorState.cursorLine,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Status bar
        EditorStatusBar(
            line = editorState.cursorLine + 1,
            column = editorState.cursorColumn + 1,
            language = editorState.language
        )
    }
}

@Composable
fun EditorTabBar(
    fileName: String,
    filePath: String,
    language: String,
    isModified: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(CursorSurfaceVariant)
            .padding(horizontal = 4.dp)
    ) {
        // Active tab
        Surface(
            color = TabActive,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Language icon
                Icon(
                    imageVector = when {
                        language.contains("typescript") || language.contains("tsx") -> Icons.Rounded.Code
                        language.contains("javascript") -> Icons.Rounded.Javascript
                        language.contains("python") -> Icons.Rounded.Code
                        language.contains("json") -> Icons.Rounded.DataObject
                        language.contains("markdown") -> Icons.Rounded.Description
                        else -> Icons.Rounded.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = when {
                        language.contains("typescript") -> SyntaxNumber
                        language.contains("javascript") -> StatusWarning
                        language.contains("python") -> SyntaxFunction
                        else -> TextSecondary
                    },
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = fileName.ifBlank { "untitled" },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )

                if (isModified) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CursorPrimary)
                    )
                }
            }
        }
    }
}

@Composable
fun EditorToolbar(onAction: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(CursorSurfaceElevated)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        ToolbarButton(Icons.Rounded.Undo, "Undo") { onAction("undo") }
        ToolbarButton(Icons.Rounded.Redo, "Redo") { onAction("redo") }

        Divider(
            modifier = Modifier
                .height(20.dp)
                .width(1.dp),
            color = BorderDefault
        )

        ToolbarButton(Icons.Rounded.ContentCopy, "Copy") { onAction("copy") }
        ToolbarButton(Icons.Rounded.ContentCut, "Cut") { onAction("cut") }
        ToolbarButton(Icons.Rounded.ContentPaste, "Paste") { onAction("paste") }

        Divider(
            modifier = Modifier
                .height(20.dp)
                .width(1.dp),
            color = BorderDefault
        )

        ToolbarButton(Icons.Rounded.Search, "Find") { onAction("find") }
        ToolbarButton(Icons.Rounded.FindReplace, "Replace") { onAction("replace") }

        Spacer(Modifier.weight(1f))

        ToolbarButton(Icons.Rounded.AutoAwesome, "AI") { onAction("cursor.compose") }
        ToolbarButton(Icons.Rounded.FormatAlignLeft, "Format") { onAction("format") }
        ToolbarButton(Icons.Rounded.Save, "Save") { onAction("save") }
    }
}

@Composable
fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun LineNumberGutter(
    content: String,
    currentLine: Int,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
            .width(48.dp)
            .background(EditorGutter)
            .verticalScroll(scrollState)
            .padding(end = 8.dp, top = 4.dp)
    ) {
        lines.forEachIndexed { index, _ ->
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 20.sp
                ),
                color = if (index == currentLine) TextPrimary else TextTertiary
            )
        }
    }
}

@Composable
fun CodeContent(
    content: String,
    language: String,
    currentLine: Int,
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val lines = content.lines()

    Column(
        modifier = modifier
            .background(EditorBackground)
            .verticalScroll(verticalScroll)
            .horizontalScroll(horizontalScroll)
            .padding(start = 8.dp, top = 4.dp, end = 16.dp)
    ) {
        lines.forEachIndexed { index, line ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (index == currentLine) {
                            Modifier.background(EditorLineHighlight)
                        } else {
                            Modifier
                        }
                    )
            ) {
                Text(
                    text = highlightSyntax(line, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EditorStatusBar(
    line: Int,
    column: Int,
    language: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(CursorPrimaryVariant)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ln $line, Col $column",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary.copy(alpha = 0.8f)
            )
            Text(
                text = "UTF-8",
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary.copy(alpha = 0.8f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "Plain Text" },
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

// Simple syntax highlighter
private fun highlightSyntax(
    line: String,
    language: String
): androidx.compose.ui.text.AnnotatedString {
    val keywords = setOf(
        "import", "from", "export", "const", "let", "var", "function",
        "return", "if", "else", "for", "while", "class", "interface",
        "type", "extends", "implements", "new", "this", "super",
        "async", "await", "try", "catch", "finally", "throw",
        "default", "switch", "case", "break", "continue",
        "public", "private", "protected", "static", "readonly",
        "void", "null", "undefined", "true", "false",
        "React", "useState", "useEffect", "useCallback", "useMemo"
    )

    val types = setOf(
        "string", "number", "boolean", "any", "never", "object",
        "String", "Number", "Boolean", "Array", "Promise", "FC"
    )

    return buildAnnotatedString {
        var i = 0
        val text = line

        while (i < text.length) {
            when {
                // Comments
                text.startsWith("//", i) -> {
                    withStyle(SpanStyle(color = SyntaxComment)) {
                        append(text.substring(i))
                    }
                    i = text.length
                }

                // Strings (double quotes)
                text[i] == '"' || text[i] == '\'' || text[i] == '`' -> {
                    val quote = text[i]
                    val end = text.indexOf(quote, i + 1).let {
                        if (it == -1) text.length else it + 1
                    }
                    withStyle(SpanStyle(color = SyntaxString)) {
                        append(text.substring(i, end))
                    }
                    i = end
                }

                // JSX tags
                text[i] == '<' && i + 1 < text.length && (text[i + 1].isLetter() || text[i + 1] == '/') -> {
                    val end = text.indexOf('>', i).let {
                        if (it == -1) text.length else it + 1
                    }
                    withStyle(SpanStyle(color = SyntaxKeyword)) {
                        append(text.substring(i, end))
                    }
                    i = end
                }

                // Words
                text[i].isLetter() || text[i] == '_' -> {
                    val start = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) i++
                    val word = text.substring(start, i)

                    when {
                        word in keywords -> {
                            withStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Medium)) {
                                append(word)
                            }
                        }
                        word in types -> {
                            withStyle(SpanStyle(color = SyntaxType)) {
                                append(word)
                            }
                        }
                        word[0].isUpperCase() -> {
                            withStyle(SpanStyle(color = SyntaxType)) {
                                append(word)
                            }
                        }
                        i < text.length && text[i] == '(' -> {
                            withStyle(SpanStyle(color = SyntaxFunction)) {
                                append(word)
                            }
                        }
                        else -> {
                            withStyle(SpanStyle(color = TextPrimary)) {
                                append(word)
                            }
                        }
                    }
                }

                // Numbers
                text[i].isDigit() -> {
                    val start = i
                    while (i < text.length && (text[i].isDigit() || text[i] == '.')) i++
                    withStyle(SpanStyle(color = SyntaxNumber)) {
                        append(text.substring(start, i))
                    }
                }

                // Operators and symbols
                else -> {
                    val color = when (text[i]) {
                        '{', '}', '(', ')', '[', ']' -> TextPrimary
                        '=', '+', '-', '*', '/', '&', '|', '!' -> SyntaxKeyword
                        ':', ';', ',' -> TextSecondary
                        '.' -> TextSecondary
                        else -> TextPrimary
                    }
                    withStyle(SpanStyle(color = color)) {
                        append(text[i])
                    }
                    i++
                }
            }
        }
    }
}
