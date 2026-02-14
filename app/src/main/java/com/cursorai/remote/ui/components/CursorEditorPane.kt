package com.cursorai.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.EditorState
import com.cursorai.remote.data.model.EditorTab
import com.cursorai.remote.ui.theme.*

/**
 * Full editor pane for tablet: tab bar + breadcrumbs + code + minimap.
 * Mimics Cursor IDE editor area pixel-perfectly.
 */
@Composable
fun CursorEditorPane(
    editorState: EditorState,
    openTabs: List<EditorTab>,
    activeTabIndex: Int,
    onTabSelect: (Int) -> Unit,
    onTabClose: (Int) -> Unit,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Tab bar
        EditorTabStrip(
            tabs = openTabs,
            activeIndex = activeTabIndex,
            onSelect = onTabSelect,
            onClose = onTabClose
        )

        // Breadcrumbs
        Breadcrumbs(filePath = editorState.filePath)

        // Editor body: gutter + code + minimap
        Row(modifier = Modifier.weight(1f)) {
            // Fold gutter (thin)
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight()
                    .background(EditorBackground)
            )

            // Line numbers
            CursorLineGutter(
                content = editorState.content,
                currentLine = editorState.cursorLine,
                modifier = Modifier.fillMaxHeight()
            )

            // Code area
            CursorCodeArea(
                content = editorState.content,
                language = editorState.language,
                currentLine = editorState.cursorLine,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Minimap
            Minimap(
                content = editorState.content,
                currentLine = editorState.cursorLine,
                totalLines = editorState.content.lines().size,
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
            )

            // Scrollbar track
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .fillMaxHeight()
                    .background(EditorBackground)
            ) {
                // Thumb
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(40.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 20.dp, end = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ScrollbarThumb)
                )
            }
        }
    }
}

@Composable
fun EditorTabStrip(
    tabs: List<EditorTab>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    onClose: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(35.dp)
            .background(EditorGroupHeader)
            .horizontalScroll(rememberScrollState())
    ) {
        tabs.forEachIndexed { index, tab ->
            val isActive = index == activeIndex

            Box(
                modifier = Modifier
                    .height(35.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(index) }
                    )
                    .background(if (isActive) TabActiveBg else TabInactiveBg)
                    .then(
                        if (isActive) {
                            Modifier.drawBehind {
                                // Top active border (purple line)
                                drawLine(
                                    color = TabActiveBorder,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 2f
                                )
                            }
                        } else Modifier
                    )
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // File type icon
                    Icon(
                        imageVector = getLanguageIcon(tab.language),
                        contentDescription = null,
                        tint = getLanguageColor(tab.language),
                        modifier = Modifier.size(14.dp)
                    )

                    // File name
                    Text(
                        text = tab.fileName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = if (isActive) TabActiveFg else TabInactiveFg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Modified dot
                    if (tab.isModified) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(TextPrimary)
                        )
                    }

                    // Close button
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = if (isActive) TabInactiveFg else TabInactiveFg.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .clickable { onClose(index) }
                    )
                }
            }

            // Tab separator
            if (!isActive && index != activeIndex - 1) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(35.dp)
                        .padding(vertical = 8.dp)
                        .background(PanelBorder)
                )
            }
        }

        // Fill remaining space
        Box(
            modifier = Modifier
                .weight(1f)
                .height(35.dp)
                .background(EditorGroupHeader)
        )
    }
}

@Composable
fun Breadcrumbs(
    filePath: String,
    modifier: Modifier = Modifier
) {
    val parts = filePath.split("/").filter { it.isNotBlank() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(EditorBackground)
            .padding(horizontal = 12.dp)
    ) {
        parts.forEachIndexed { index, part ->
            if (index > 0) {
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = BreadcrumbSeparator,
                    modifier = Modifier.size(14.dp)
                )
            }

            val isLast = index == parts.lastIndex
            Text(
                text = part,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = if (isLast) BreadcrumbActiveFg else BreadcrumbFg
            )
        }
    }
}

@Composable
fun CursorLineGutter(
    content: String,
    currentLine: Int,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
            .width(44.dp)
            .background(EditorBackground)
            .verticalScroll(scrollState)
            .padding(end = 12.dp, start = 4.dp, top = 0.dp)
    ) {
        lines.forEachIndexed { index, _ ->
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 19.sp
                ),
                color = if (index == currentLine) TextPrimary else TextTertiary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CursorCodeArea(
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
            .padding(start = 0.dp, end = 4.dp)
    ) {
        lines.forEachIndexed { index, line ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (index == currentLine) {
                            Modifier.background(EditorLineHighlight)
                        } else Modifier
                    )
            ) {
                Text(
                    text = highlightCode(line, language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 19.sp
                    )
                )
            }
        }
    }
}

@Composable
fun Minimap(
    content: String,
    currentLine: Int,
    totalLines: Int,
    modifier: Modifier = Modifier
) {
    val lines = content.lines()

    Box(
        modifier = modifier
            .background(MinimapBg)
            .padding(horizontal = 4.dp)
    ) {
        // Viewport indicator
        val viewportTop = if (totalLines > 0) (currentLine.toFloat() / totalLines.coerceAtLeast(1)) else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .offset(y = (viewportTop * 200).dp)
                .background(MinimapSlider)
        )

        // Minimap lines (very tiny representations)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            lines.forEachIndexed { index, line ->
                if (index < 80) { // Only render ~80 lines for perf
                    val trimmed = line.trimStart()
                    val indent = line.length - trimmed.length
                    val lineWidth = (trimmed.length.coerceAtMost(60) * 0.8f).dp

                    if (trimmed.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(start = (indent * 1.5f).dp)
                                .height(2.dp)
                                .width(lineWidth)
                                .alpha(if (index == currentLine) 0.8f else 0.35f)
                                .background(
                                    when {
                                        trimmed.startsWith("//") || trimmed.startsWith("#") -> SyntaxComment
                                        trimmed.startsWith("import") || trimmed.startsWith("export") -> SyntaxKeyword
                                        trimmed.contains("\"") || trimmed.contains("'") -> SyntaxString
                                        else -> TextPrimary
                                    }
                                )
                        )
                    }
                    Spacer(Modifier.height(1.dp))
                }
            }
        }
    }
}

// Helper functions
private fun getLanguageIcon(language: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        language.contains("typescript") || language.contains("tsx") -> Icons.Rounded.Code
        language.contains("javascript") || language.contains("jsx") -> Icons.Rounded.Javascript
        language.contains("json") -> Icons.Rounded.DataObject
        language.contains("markdown") -> Icons.Rounded.Description
        language.contains("css") || language.contains("scss") -> Icons.Rounded.Palette
        language.contains("html") -> Icons.Rounded.Language
        else -> Icons.Rounded.InsertDriveFile
    }
}

private fun getLanguageColor(language: String): Color {
    return when {
        language.contains("typescript") -> SyntaxNumber
        language.contains("javascript") -> StatusWarning
        language.contains("json") -> StatusWarning
        language.contains("css") -> SyntaxKeyword
        language.contains("html") -> SyntaxKeyword
        language.contains("markdown") -> SyntaxFunction
        else -> TextTertiary
    }
}

// Syntax highlighter (reused from EditorScreen but cleaner)
private fun highlightCode(
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
                text.startsWith("//", i) -> {
                    withStyle(SpanStyle(color = SyntaxComment)) { append(text.substring(i)) }
                    i = text.length
                }
                text[i] == '"' || text[i] == '\'' || text[i] == '`' -> {
                    val quote = text[i]
                    val end = text.indexOf(quote, i + 1).let { if (it == -1) text.length else it + 1 }
                    withStyle(SpanStyle(color = SyntaxString)) { append(text.substring(i, end)) }
                    i = end
                }
                text[i] == '<' && i + 1 < text.length && (text[i + 1].isLetter() || text[i + 1] == '/') -> {
                    val end = text.indexOf('>', i).let { if (it == -1) text.length else it + 1 }
                    withStyle(SpanStyle(color = SyntaxKeyword)) { append(text.substring(i, end)) }
                    i = end
                }
                text[i].isLetter() || text[i] == '_' -> {
                    val start = i
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) i++
                    val word = text.substring(start, i)
                    when {
                        word in keywords -> withStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Medium)) { append(word) }
                        word in types -> withStyle(SpanStyle(color = SyntaxType)) { append(word) }
                        word[0].isUpperCase() -> withStyle(SpanStyle(color = SyntaxType)) { append(word) }
                        i < text.length && text[i] == '(' -> withStyle(SpanStyle(color = SyntaxFunction)) { append(word) }
                        else -> withStyle(SpanStyle(color = TextPrimary)) { append(word) }
                    }
                }
                text[i].isDigit() -> {
                    val start = i
                    while (i < text.length && (text[i].isDigit() || text[i] == '.')) i++
                    withStyle(SpanStyle(color = SyntaxNumber)) { append(text.substring(start, i)) }
                }
                else -> {
                    val color = when (text[i]) {
                        '{', '}', '(', ')', '[', ']' -> TextPrimary
                        '=', '+', '-', '*', '/', '&', '|', '!' -> SyntaxKeyword
                        ':', ';', ',' -> TextSecondary
                        '.' -> TextSecondary
                        else -> TextPrimary
                    }
                    withStyle(SpanStyle(color = color)) { append(text[i]) }
                    i++
                }
            }
        }
    }
}
