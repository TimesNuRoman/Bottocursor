package com.cursorai.remote.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.*
import com.cursorai.remote.ui.theme.*

/**
 * R2 Object Storage browser — visual file manager for Cloudflare R2.
 * Lists buckets → browse objects → preview/download/delete.
 */
@Composable
fun R2BrowserPanel(
    buckets: List<R2Bucket>,
    objects: List<R2Object>,
    currentBucket: String,
    currentPrefix: String,
    isLoading: Boolean,
    previewContent: String,
    previewKey: String,
    onSelectBucket: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onRefresh: () -> Unit,
    onPreview: (String) -> Unit,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismissPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Toolbar
        R2Toolbar(
            currentBucket = currentBucket,
            currentPrefix = currentPrefix,
            isLoading = isLoading,
            onBack = {
                if (currentPrefix.isNotBlank()) {
                    val parent = currentPrefix.trimEnd('/').substringBeforeLast('/', "")
                    onNavigate(if (parent.isBlank()) "" else "$parent/")
                } else {
                    onSelectBucket("")
                }
            },
            onRefresh = onRefresh
        )

        // Breadcrumb path
        if (currentBucket.isNotBlank()) {
            R2Breadcrumb(
                bucket = currentBucket,
                prefix = currentPrefix,
                onNavigate = onNavigate,
                onSelectBucket = { onSelectBucket("") }
            )
        }

        // Preview overlay
        if (previewKey.isNotBlank()) {
            R2PreviewCard(
                objectKey = previewKey,
                content = previewContent,
                onDismiss = onDismissPreview,
                onDownload = { onDownload(previewKey) }
            )
        }

        // Content
        if (currentBucket.isBlank()) {
            // Bucket list
            BucketList(
                buckets = buckets,
                isLoading = isLoading,
                onSelect = onSelectBucket
            )
        } else {
            // Object list
            ObjectList(
                objects = objects,
                isLoading = isLoading,
                onNavigate = onNavigate,
                onPreview = onPreview,
                onDownload = onDownload,
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun R2Toolbar(
    currentBucket: String,
    currentPrefix: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(PanelBg)
            .padding(horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(16.dp).clip(RoundedCornerShape(3.dp)).background(R2Purple.copy(alpha = 0.15f))
        ) {
            Icon(Icons.Rounded.Storage, null, tint = R2Purple, modifier = Modifier.size(10.dp))
        }
        Spacer(Modifier.width(6.dp))
        Text(
            if (currentBucket.isBlank()) "R2 Buckets" else currentBucket,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = if (currentBucket.isBlank()) TextSecondary else R2Purple
        )

        if (isLoading) {
            Spacer(Modifier.width(6.dp))
            CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = R2Purple)
        }

        Spacer(Modifier.weight(1f))

        if (currentBucket.isNotBlank()) {
            IconButton(onClick = onBack, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = PanelTabInactive, modifier = Modifier.size(13.dp))
            }
        }
        IconButton(onClick = onRefresh, modifier = Modifier.size(22.dp)) {
            Icon(Icons.Rounded.Refresh, "Refresh", tint = PanelTabInactive, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
fun R2Breadcrumb(
    bucket: String,
    prefix: String,
    onNavigate: (String) -> Unit,
    onSelectBucket: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(PanelHeaderBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp)
    ) {
        Text("R2", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary,
            modifier = Modifier.clickable { onSelectBucket() })
        Icon(Icons.Rounded.ChevronRight, null, tint = BreadcrumbSeparator, modifier = Modifier.size(12.dp))
        Text(bucket, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = R2Purple,
            modifier = Modifier.clickable { onNavigate("") })

        if (prefix.isNotBlank()) {
            val parts = prefix.trimEnd('/').split('/')
            var accumulated = ""
            parts.forEach { part ->
                accumulated += "$part/"
                val nav = accumulated
                Icon(Icons.Rounded.ChevronRight, null, tint = BreadcrumbSeparator, modifier = Modifier.size(12.dp))
                Text(part, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (nav == prefix) TextPrimary else BreadcrumbFg,
                    modifier = Modifier.clickable { onNavigate(nav) })
            }
        }
    }
}

@Composable
fun BucketList(buckets: List<R2Bucket>, isLoading: Boolean, onSelect: (String) -> Unit) {
    if (buckets.isEmpty() && !isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Storage, null, tint = R2Purple.copy(alpha = 0.3f), modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("No R2 buckets found", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Text("Run: wrangler r2 bucket create <name>", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp), color = TextTertiary)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(buckets) { bucket ->
                Surface(
                    onClick = { onSelect(bucket.name) },
                    shape = RoundedCornerShape(6.dp),
                    color = CursorSurfaceElevated,
                    border = BorderStroke(1.dp, PanelBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(R2Purple.copy(alpha = 0.12f))
                        ) {
                            Icon(Icons.Rounded.Inventory2, null, tint = R2Purple, modifier = Modifier.size(15.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(bucket.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = TextPrimary)
                            if (bucket.createdAt.isNotBlank()) {
                                Text(bucket.createdAt, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                            }
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ObjectList(
    objects: List<R2Object>,
    isLoading: Boolean,
    onNavigate: (String) -> Unit,
    onPreview: (String) -> Unit,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (objects.isEmpty() && !isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Empty", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
    } else {
        // Column headers
        Row(
            modifier = Modifier.fillMaxWidth().height(22.dp).background(PanelHeaderBg).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Name", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold), color = TextTertiary, modifier = Modifier.weight(1f))
            Text("Size", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold), color = TextTertiary, modifier = Modifier.width(60.dp))
            Text("Modified", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold), color = TextTertiary, modifier = Modifier.width(80.dp))
            Spacer(Modifier.width(60.dp)) // Actions
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(objects) { obj ->
                R2ObjectRow(
                    obj = obj,
                    onNavigate = onNavigate,
                    onPreview = onPreview,
                    onDownload = onDownload,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun R2ObjectRow(
    obj: R2Object,
    onNavigate: (String) -> Unit,
    onPreview: (String) -> Unit,
    onDownload: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable {
                if (obj.isFolder) onNavigate(obj.key) else onPreview(obj.key)
            }
            .padding(horizontal = 12.dp)
    ) {
        // Icon
        Icon(
            imageVector = when {
                obj.isFolder -> Icons.Rounded.Folder
                obj.extension in listOf("jpg", "jpeg", "png", "gif", "svg", "webp") -> Icons.Rounded.Image
                obj.extension in listOf("pdf") -> Icons.Rounded.PictureAsPdf
                obj.extension in listOf("json", "xml", "csv") -> Icons.Rounded.DataObject
                obj.extension in listOf("js", "ts", "py", "go", "rs") -> Icons.Rounded.Code
                obj.extension in listOf("zip", "tar", "gz") -> Icons.Rounded.FolderZip
                obj.extension in listOf("mp4", "mov", "avi") -> Icons.Rounded.Videocam
                obj.extension in listOf("mp3", "wav", "ogg") -> Icons.Rounded.MusicNote
                else -> Icons.Rounded.InsertDriveFile
            },
            contentDescription = null,
            tint = when {
                obj.isFolder -> StatusWarning.copy(alpha = 0.8f)
                obj.extension in listOf("jpg", "jpeg", "png", "gif", "svg", "webp") -> R2Purple
                obj.extension in listOf("json") -> StatusWarning
                obj.extension in listOf("js", "ts") -> SyntaxNumber
                else -> TextTertiary
            },
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))

        // Name
        Text(
            obj.fileName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = if (obj.isFolder) TextPrimary else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Size
        Text(
            if (obj.isFolder) "-" else obj.sizeFormatted,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextTertiary,
            modifier = Modifier.width(60.dp)
        )

        // Date
        Text(
            obj.lastModified.take(10),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextTertiary,
            modifier = Modifier.width(80.dp)
        )

        // Actions
        if (!obj.isFolder) {
            IconButton(onClick = { onDownload(obj.key) }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.Download, "Download", tint = TextTertiary, modifier = Modifier.size(12.dp))
            }
            IconButton(onClick = { onDelete(obj.key) }, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.DeleteOutline, "Delete", tint = StatusError.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
            }
        } else {
            Spacer(Modifier.width(60.dp))
        }
    }
}

@Composable
fun R2PreviewCard(
    objectKey: String,
    content: String,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CursorSurfaceElevated,
        border = BorderStroke(1.dp, R2Purple.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Visibility, null, tint = R2Purple, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(objectKey.substringAfterLast('/'), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = onDownload, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Rounded.Download, "Download", tint = R2Purple, modifier = Modifier.size(12.dp))
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Rounded.Close, "Close", tint = TextTertiary, modifier = Modifier.size(12.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                content.take(2000),
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp, color = TerminalText),
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }
}
