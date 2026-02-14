package com.cursorai.remote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cursorai.remote.data.model.FileNode
import com.cursorai.remote.ui.theme.*

@Composable
fun FileExplorerScreen(
    fileTree: List<FileNode>,
    onFileSelect: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CursorSurface)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(CursorSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "EXPLORER",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { /* collapse all */ },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Rounded.UnfoldLess,
                        contentDescription = "Collapse All",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // File tree
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(fileTree) { node ->
                FileTreeItem(
                    node = node,
                    depth = 0,
                    onFileSelect = onFileSelect
                )
            }
        }
    }
}

@Composable
fun FileTreeItem(
    node: FileNode,
    depth: Int,
    onFileSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(depth < 1) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (node.isDirectory) {
                        expanded = !expanded
                    } else {
                        onFileSelect(node.path)
                    }
                }
                .padding(start = (16 + depth * 16).dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
        ) {
            if (node.isDirectory) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowDown
                    else Icons.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(Modifier.width(4.dp))

                Icon(
                    imageVector = if (expanded) Icons.Rounded.FolderOpen else Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = StatusWarning,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Spacer(Modifier.width(20.dp))

                Icon(
                    imageVector = getFileIcon(node.extension),
                    contentDescription = null,
                    tint = getFileColor(node.extension),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = node.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (node.isDirectory) TextPrimary else TextSecondary
            )
        }

        // Children
        AnimatedVisibility(
            visible = expanded && node.isDirectory,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                node.children.forEach { child ->
                    FileTreeItem(
                        node = child,
                        depth = depth + 1,
                        onFileSelect = onFileSelect
                    )
                }
            }
        }
    }
}

private fun getFileIcon(extension: String): ImageVector {
    return when (extension.lowercase()) {
        "ts", "tsx" -> Icons.Rounded.Code
        "js", "jsx" -> Icons.Rounded.Javascript
        "json" -> Icons.Rounded.DataObject
        "md" -> Icons.Rounded.Description
        "css", "scss", "sass" -> Icons.Rounded.Palette
        "html" -> Icons.Rounded.Language
        "png", "jpg", "svg", "gif" -> Icons.Rounded.Image
        "kt", "kts" -> Icons.Rounded.Code
        "py" -> Icons.Rounded.Code
        "xml" -> Icons.Rounded.Code
        "yaml", "yml" -> Icons.Rounded.Settings
        "gradle" -> Icons.Rounded.Build
        "gitignore" -> Icons.Rounded.VisibilityOff
        else -> Icons.Rounded.InsertDriveFile
    }
}

private fun getFileColor(extension: String): androidx.compose.ui.graphics.Color {
    return when (extension.lowercase()) {
        "ts", "tsx" -> SyntaxNumber
        "js", "jsx" -> StatusWarning
        "json" -> StatusWarning
        "md" -> SyntaxFunction
        "css", "scss" -> SyntaxKeyword
        "html" -> SyntaxKeyword
        "kt", "kts" -> SyntaxFunction
        "py" -> SyntaxFunction
        "xml" -> SyntaxType
        else -> TextTertiary
    }
}
