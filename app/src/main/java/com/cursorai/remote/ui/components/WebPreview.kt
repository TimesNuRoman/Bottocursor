package com.cursorai.remote.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.cursorai.remote.data.model.ConsoleLogEntry
import com.cursorai.remote.data.model.PreviewDevice
import com.cursorai.remote.ui.theme.*

/**
 * Full browser preview with DevTools-like toolbar.
 * Supports responsive device simulation, URL navigation, refresh, console.
 */
@Composable
fun WebPreviewPanel(
    url: String,
    isDevServerRunning: Boolean,
    consoleLogs: List<ConsoleLogEntry>,
    selectedDevice: PreviewDevice,
    onUrlChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onDeviceChange: (PreviewDevice) -> Unit,
    onStartDevServer: () -> Unit,
    onStopDevServer: () -> Unit,
    onConsoleClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentUrl by remember(url) { mutableStateOf(url) }
    var editingUrl by remember { mutableStateOf(false) }
    var urlInput by remember(url) { mutableStateOf(url) }
    var isLoading by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf("") }
    var showDeviceDropdown by remember { mutableStateOf(false) }
    var showConsole by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Browser toolbar
        PreviewToolbar(
            url = if (editingUrl) urlInput else currentUrl,
            isLoading = isLoading,
            isEditing = editingUrl,
            isDevServerRunning = isDevServerRunning,
            selectedDevice = selectedDevice,
            consoleCount = consoleLogs.size,
            onUrlChange = { urlInput = it },
            onUrlSubmit = {
                editingUrl = false
                val newUrl = if (urlInput.startsWith("http")) urlInput else "http://$urlInput"
                currentUrl = newUrl
                onUrlChange(newUrl)
                webViewRef?.loadUrl(newUrl)
            },
            onUrlFocus = { editingUrl = true },
            onBack = { webViewRef?.goBack() },
            onForward = { webViewRef?.goForward() },
            onRefresh = {
                webViewRef?.reload()
                onRefresh()
            },
            onDeviceSelect = { showDeviceDropdown = !showDeviceDropdown },
            onToggleConsole = { showConsole = !showConsole },
            onStartDevServer = onStartDevServer,
            onStopDevServer = onStopDevServer
        )

        // Device dropdown
        if (showDeviceDropdown) {
            DeviceSelector(
                selectedDevice = selectedDevice,
                onSelect = {
                    onDeviceChange(it)
                    showDeviceDropdown = false
                },
                onDismiss = { showDeviceDropdown = false }
            )
        }

        // WebView area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF292929))
        ) {
            if (currentUrl.isNotBlank()) {
                // Device frame wrapper
                val deviceMod = if (selectedDevice != PreviewDevice.RESPONSIVE && selectedDevice.width > 0) {
                    Modifier
                        .widthIn(max = selectedDevice.width.dp)
                        .fillMaxHeight()
                        .border(1.dp, PanelBorder, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                } else {
                    Modifier.fillMaxSize()
                }

                Box(modifier = deviceMod) {
                    WebViewComposable(
                        url = currentUrl,
                        onPageStarted = { isLoading = true },
                        onPageFinished = { title ->
                            isLoading = false
                            pageTitle = title
                        },
                        onWebViewCreated = { webViewRef = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Device label
                if (selectedDevice != PreviewDevice.RESPONSIVE) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${selectedDevice.label} (${selectedDevice.width}x${selectedDevice.height})",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                // No URL / server not running
                EmptyPreviewState(
                    isDevServerRunning = isDevServerRunning,
                    onStartDevServer = onStartDevServer
                )
            }
        }

        // Console panel
        if (showConsole) {
            ConsolePanel(
                logs = consoleLogs,
                onClear = onConsoleClear,
                onClose = { showConsole = false }
            )
        }
    }
}

@Composable
fun PreviewToolbar(
    url: String,
    isLoading: Boolean,
    isEditing: Boolean,
    isDevServerRunning: Boolean,
    selectedDevice: PreviewDevice,
    consoleCount: Int,
    onUrlChange: (String) -> Unit,
    onUrlSubmit: () -> Unit,
    onUrlFocus: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onDeviceSelect: () -> Unit,
    onToggleConsole: () -> Unit,
    onStartDevServer: () -> Unit,
    onStopDevServer: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(PanelHeaderBg)
                .padding(horizontal = 6.dp)
        ) {
            // Nav buttons
            IconButton(onClick = onBack, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = PanelTabInactive, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onForward, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Rounded.ArrowForward, "Forward", tint = PanelTabInactive, modifier = Modifier.size(14.dp))
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(26.dp)) {
                Icon(
                    if (isLoading) Icons.Rounded.Close else Icons.Rounded.Refresh,
                    "Refresh", tint = PanelTabInactive, modifier = Modifier.size(14.dp)
                )
            }

            // URL bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CursorSurfaceElevated)
                    .clickable { onUrlFocus() }
                    .padding(horizontal = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (url.startsWith("https")) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    if (isEditing) {
                        BasicTextField(
                            value = url,
                            onValueChange = onUrlChange,
                            textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary, fontFamily = FontFamily.Default),
                            cursorBrush = SolidColor(CursorPrimary),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = url.ifBlank { "Enter URL or start dev server..." },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = if (url.isBlank()) TextTertiary else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Loading indicator
                if (isLoading) {
                    LinearProgressIndicator(
                        color = CursorPrimary,
                        trackColor = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }

            // Device toggle
            IconButton(onClick = onDeviceSelect, modifier = Modifier.size(26.dp)) {
                Icon(
                    Icons.Outlined.Devices,
                    "Devices",
                    tint = if (selectedDevice != PreviewDevice.RESPONSIVE) CursorPrimary else PanelTabInactive,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Console toggle
            Box(modifier = Modifier.size(26.dp)) {
                IconButton(onClick = onToggleConsole, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Outlined.Code, "Console", tint = PanelTabInactive, modifier = Modifier.size(14.dp))
                }
                if (consoleCount > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusError)
                    ) {
                        Text(
                            "$consoleCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                            color = Color.White
                        )
                    }
                }
            }

            // Separator
            Box(Modifier.width(1.dp).height(18.dp).background(PanelBorder))
            Spacer(Modifier.width(4.dp))

            // Dev server control
            if (isDevServerRunning) {
                IconButton(onClick = onStopDevServer, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Rounded.Stop, "Stop server", tint = StatusError, modifier = Modifier.size(15.dp))
                }
            } else {
                IconButton(onClick = onStartDevServer, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Rounded.PlayArrow, "Start server", tint = StatusSuccess, modifier = Modifier.size(15.dp))
                }
            }

            // Open external
            IconButton(onClick = { /* open in system browser */ }, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Outlined.OpenInNew, "Open external", tint = PanelTabInactive, modifier = Modifier.size(13.dp))
            }
        }
    }
}

@Composable
fun DeviceSelector(
    selectedDevice: PreviewDevice,
    onSelect: (PreviewDevice) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CursorSurfaceElevated,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "Device Simulation",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp
                ),
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            PreviewDevice.entries.forEach { device ->
                val isSelected = device == selectedDevice
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) ListActiveSelectionBg else Color.Transparent)
                        .clickable { onSelect(device) }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = when (device) {
                            PreviewDevice.RESPONSIVE -> Icons.Outlined.Fullscreen
                            PreviewDevice.IPHONE_SE, PreviewDevice.IPHONE_14, PreviewDevice.IPHONE_14_PRO_MAX ->
                                Icons.Outlined.PhoneIphone
                            PreviewDevice.PIXEL_7 -> Icons.Outlined.PhoneAndroid
                            PreviewDevice.IPAD, PreviewDevice.IPAD_PRO -> Icons.Outlined.Tablet
                            PreviewDevice.DESKTOP_HD, PreviewDevice.DESKTOP_4K -> Icons.Outlined.DesktopWindows
                        },
                        contentDescription = null,
                        tint = if (isSelected) CursorPrimary else TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        device.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                    if (device.width > 0) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${device.width}x${device.height}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPreviewState(
    isDevServerRunning: Boolean,
    onStartDevServer: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Outlined.Language,
            contentDescription = null,
            tint = TextTertiary.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Web Preview",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (isDevServerRunning) "Enter a URL in the address bar"
            else "Start a dev server to preview your app",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
        if (!isDevServerRunning) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onStartDevServer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CursorPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Start Dev Server", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ConsolePanel(
    logs: List<ConsoleLogEntry>,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(PanelBg)
    ) {
        // Console header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(PanelHeaderBg)
                .padding(horizontal = 8.dp)
        ) {
            Text("CONSOLE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp), color = PanelTabActive)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClear, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Rounded.Delete, "Clear", tint = PanelTabInactive, modifier = Modifier.size(11.dp))
            }
            IconButton(onClick = onClose, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Rounded.Close, "Close", tint = PanelTabInactive, modifier = Modifier.size(11.dp))
            }
        }

        // Console logs
        if (logs.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No console output", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = TextTertiary)
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                logs.takeLast(20).forEach { log ->
                    Text(
                        text = log.message,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp,
                            color = when (log.level) {
                                "error" -> StatusError
                                "warn" -> StatusWarning
                                "info" -> StatusInfo
                                else -> TerminalText
                            }
                        )
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComposable(
    url: String,
    onPageStarted: () -> Unit,
    onPageFinished: (String) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // Enable remote debugging
                WebView.setWebContentsDebuggingEnabled(true)

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onPageStarted()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onPageFinished(view?.title ?: "")
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        return true
                    }
                }

                onWebViewCreated(this)
                if (url.isNotBlank()) {
                    loadUrl(url)
                }
            }
        },
        update = { webView ->
            if (url.isNotBlank() && webView.url != url) {
                webView.loadUrl(url)
            }
        },
        modifier = modifier
    )
}
