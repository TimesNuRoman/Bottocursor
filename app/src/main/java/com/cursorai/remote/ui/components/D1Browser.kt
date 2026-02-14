package com.cursorai.remote.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cursorai.remote.data.model.*
import com.cursorai.remote.ui.theme.*

/**
 * D1 Database browser — SQL explorer for Cloudflare D1.
 * List databases → tables → schema / query / results.
 */
@Composable
fun D1BrowserPanel(
    databases: List<D1Database>,
    tables: List<D1Table>,
    columns: List<D1Column>,
    queryResult: D1QueryResult?,
    currentDatabase: String,
    currentTable: String,
    isLoading: Boolean,
    onSelectDatabase: (String) -> Unit,
    onSelectTable: (String) -> Unit,
    onExecuteQuery: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sqlInput by remember { mutableStateOf("") }
    var showSchema by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Toolbar
        D1Toolbar(
            currentDatabase = currentDatabase,
            currentTable = currentTable,
            isLoading = isLoading,
            onBack = {
                if (currentTable.isNotBlank()) onSelectTable("")
                else onSelectDatabase("")
            },
            onRefresh = onRefresh,
            onToggleSchema = { showSchema = !showSchema }
        )

        if (currentDatabase.isBlank()) {
            // Database list
            DatabaseList(databases = databases, isLoading = isLoading, onSelect = onSelectDatabase)
        } else if (currentTable.isBlank() && !showSchema) {
            // Table list for selected database
            TableList(tables = tables, isLoading = isLoading, onSelect = onSelectTable)
        } else {
            // Table view: schema + query + results
            Column(modifier = Modifier.weight(1f)) {
                // Schema view
                if (showSchema && columns.isNotEmpty()) {
                    SchemaView(tableName = currentTable, columns = columns)
                }

                // SQL query input
                SqlQueryInput(
                    sql = sqlInput,
                    onSqlChange = { sqlInput = it },
                    onExecute = { onExecuteQuery(sqlInput) },
                    tableName = currentTable,
                    onQuickQuery = { query ->
                        sqlInput = query
                        onExecuteQuery(query)
                    }
                )

                // Results
                if (queryResult != null) {
                    QueryResultView(result = queryResult, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun D1Toolbar(
    currentDatabase: String,
    currentTable: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleSchema: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(28.dp).background(PanelBg).padding(horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(16.dp).clip(RoundedCornerShape(3.dp)).background(D1Cyan.copy(alpha = 0.15f))
        ) {
            Icon(Icons.Rounded.TableChart, null, tint = D1Cyan, modifier = Modifier.size(10.dp))
        }
        Spacer(Modifier.width(6.dp))

        // Path
        Text(
            buildString {
                if (currentDatabase.isBlank()) append("D1 Databases")
                else {
                    append(currentDatabase)
                    if (currentTable.isNotBlank()) append(" > $currentTable")
                }
            },
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = if (currentDatabase.isBlank()) TextSecondary else D1Cyan
        )

        if (isLoading) {
            Spacer(Modifier.width(6.dp))
            CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = D1Cyan)
        }

        Spacer(Modifier.weight(1f))

        if (currentTable.isNotBlank()) {
            IconButton(onClick = onToggleSchema, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Outlined.Schema, "Schema", tint = PanelTabInactive, modifier = Modifier.size(13.dp))
            }
        }
        if (currentDatabase.isNotBlank()) {
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
fun DatabaseList(databases: List<D1Database>, isLoading: Boolean, onSelect: (String) -> Unit) {
    if (databases.isEmpty() && !isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.TableChart, null, tint = D1Cyan.copy(alpha = 0.3f), modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("No D1 databases found", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                Text("Run: wrangler d1 create <name>", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp), color = TextTertiary)
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(databases) { db ->
                Surface(
                    onClick = { onSelect(db.name) },
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
                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(D1Cyan.copy(alpha = 0.12f))
                        ) {
                            Icon(Icons.Rounded.Storage, null, tint = D1Cyan, modifier = Modifier.size(15.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(db.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = TextPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (db.uuid.isNotBlank()) Text(db.uuid.take(8) + "...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                                if (db.numTables > 0) Text("${db.numTables} tables", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
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
fun TableList(tables: List<D1Table>, isLoading: Boolean, onSelect: (String) -> Unit) {
    if (tables.isEmpty() && !isLoading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("No tables", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            items(tables) { table ->
                Surface(
                    onClick = { onSelect(table.name) },
                    shape = RoundedCornerShape(4.dp),
                    color = CursorSurfaceElevated
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.TableRows, null, tint = D1Cyan, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(table.name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = TextPrimary, modifier = Modifier.weight(1f))
                        if (table.rowCount > 0) {
                            Text("${table.rowCount} rows", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                        }
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Rounded.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SchemaView(tableName: String, columns: List<D1Column>) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = D1Cyan.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, D1Cyan.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Schema: $tableName", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold), color = D1Cyan)
            Spacer(Modifier.height(4.dp))
            columns.forEach { col ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (col.isPrimaryKey) {
                        Icon(Icons.Rounded.Key, null, tint = StatusWarning, modifier = Modifier.size(10.dp))
                    } else {
                        Spacer(Modifier.width(10.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(col.name, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Medium), color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(col.type, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp), color = D1Cyan)
                    if (col.isNotNull) {
                        Spacer(Modifier.width(4.dp))
                        Text("NOT NULL", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = StatusWarning)
                    }
                }
            }
        }
    }
}

@Composable
fun SqlQueryInput(
    sql: String,
    onSqlChange: (String) -> Unit,
    onExecute: () -> Unit,
    tableName: String,
    onQuickQuery: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        // Quick queries
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val queries = if (tableName.isNotBlank()) listOf(
                "SELECT *" to "SELECT * FROM $tableName LIMIT 50",
                "COUNT" to "SELECT COUNT(*) as count FROM $tableName",
                "Schema" to "PRAGMA table_info($tableName)",
            ) else listOf(
                "Tables" to "SELECT name FROM sqlite_master WHERE type='table'",
            )
            queries.forEach { (label, query) ->
                Surface(
                    onClick = { onQuickQuery(query) },
                    shape = RoundedCornerShape(4.dp),
                    color = D1Cyan.copy(alpha = 0.1f),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium), color = D1Cyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // SQL editor
        Row(verticalAlignment = Alignment.Top) {
            BasicTextField(
                value = sql,
                onValueChange = onSqlChange,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextPrimary),
                cursorBrush = SolidColor(D1Cyan),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 32.dp, max = 80.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CursorSurfaceElevated)
                    .padding(8.dp),
                decorationBox = { inner ->
                    Box {
                        if (sql.isEmpty()) Text("SELECT * FROM ...", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = TextTertiary))
                        inner()
                    }
                }
            )
            Spacer(Modifier.width(4.dp))
            Button(
                onClick = onExecute,
                enabled = sql.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = D1Cyan, disabledContainerColor = CursorSurfaceElevated),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text("Run", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
            }
        }
    }
}

@Composable
fun QueryResultView(result: D1QueryResult, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Result status bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().height(20.dp).background(PanelHeaderBg).padding(horizontal = 10.dp)
        ) {
            if (result.error.isNotBlank()) {
                Icon(Icons.Rounded.Cancel, null, tint = StatusError, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(4.dp))
                Text(result.error, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = StatusError)
            } else {
                Icon(Icons.Rounded.CheckCircle, null, tint = StatusSuccess, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(4.dp))
                Text("${result.rows.size} rows", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = StatusSuccess)
                if (result.duration > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text("${result.duration}ms", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                }
                if (result.rowsAffected > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text("${result.rowsAffected} affected", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                }
            }
        }

        if (result.columns.isNotEmpty() && result.error.isBlank()) {
            // Data table
            val hScroll = rememberScrollState()
            val vScroll = rememberScrollState()

            Column(modifier = Modifier.fillMaxSize().horizontalScroll(hScroll)) {
                // Header row
                Row(modifier = Modifier.background(SideBarSectionHeader).height(24.dp)) {
                    // Row number column
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(36.dp).fillMaxHeight().background(PanelHeaderBg)
                    ) {
                        Text("#", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold), color = TextTertiary)
                    }
                    result.columns.forEach { col ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.width(120.dp).fillMaxHeight().padding(horizontal = 6.dp)
                        ) {
                            Text(
                                col,
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = D1Cyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(Modifier.width(1.dp).fillMaxHeight().background(PanelBorder))
                    }
                }

                // Data rows
                Column(modifier = Modifier.verticalScroll(vScroll)) {
                    result.rows.forEachIndexed { rowIdx, row ->
                        val bgColor = if (rowIdx % 2 == 0) Color.Transparent else PanelBg.copy(alpha = 0.5f)
                        Row(modifier = Modifier.background(bgColor).height(22.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.width(36.dp).fillMaxHeight().background(PanelHeaderBg.copy(alpha = 0.3f))
                            ) {
                                Text("${rowIdx + 1}", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp), color = TextTertiary)
                            }
                            row.forEach { cell ->
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier.width(120.dp).fillMaxHeight().padding(horizontal = 6.dp)
                                ) {
                                    Text(
                                        cell,
                                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                        color = when {
                                            cell == "null" || cell == "NULL" -> TextTertiary
                                            cell.all { it.isDigit() || it == '.' || it == '-' } -> SyntaxNumber
                                            cell == "true" || cell == "false" -> SyntaxKeyword
                                            else -> TextPrimary
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(Modifier.width(1.dp).fillMaxHeight().background(PanelBorder.copy(alpha = 0.3f)))
                            }
                        }
                    }
                }
            }
        }
    }
}
