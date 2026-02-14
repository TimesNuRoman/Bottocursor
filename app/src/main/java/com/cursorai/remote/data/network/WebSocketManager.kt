package com.cursorai.remote.data.network

import com.cursorai.remote.data.model.ConnectionConfig
import com.cursorai.remote.data.model.ConnectionState
import com.cursorai.remote.data.model.MessageType
import com.cursorai.remote.data.model.WsMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID
import java.util.concurrent.TimeUnit

class WebSocketManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var config: ConnectionConfig? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _messages = MutableSharedFlow<WsMessage>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<WsMessage> = _messages

    private val _latency = MutableStateFlow(0L)
    val latency: StateFlow<Long> = _latency

    fun connect(connectionConfig: ConnectionConfig) {
        config = connectionConfig
        reconnectAttempts = 0
        doConnect(connectionConfig)
    }

    private fun doConnect(connectionConfig: ConnectionConfig) {
        _connectionState.value = ConnectionState.CONNECTING

        val request = Request.Builder()
            .url(connectionConfig.wsUrl)
            .apply {
                if (connectionConfig.authToken.isNotBlank()) {
                    addHeader("Authorization", "Bearer ${connectionConfig.authToken}")
                }
            }
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempts = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, WsMessage::class.java)
                    scope.launch {
                        _messages.emit(message)
                    }
                } catch (e: Exception) {
                    scope.launch {
                        _messages.emit(
                            WsMessage(
                                type = MessageType.RESPONSE,
                                payload = text,
                                id = UUID.randomUUID().toString()
                            )
                        )
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.ERROR
                attemptReconnect()
            }
        })
    }

    private fun attemptReconnect() {
        val cfg = config ?: return
        if (reconnectAttempts >= maxReconnectAttempts) return

        reconnectAttempts++
        scope.launch {
            delay(reconnectAttempts * 2000L)
            doConnect(cfg)
        }
    }

    fun send(message: WsMessage) {
        val json = gson.toJson(message)
        webSocket?.send(json)
    }

    fun sendCommand(command: String) {
        send(
            WsMessage(
                type = MessageType.COMMAND,
                payload = command,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun sendVoiceCommand(text: String) {
        send(
            WsMessage(
                type = MessageType.VOICE_COMMAND,
                payload = text,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun sendAIPrompt(prompt: String) {
        send(
            WsMessage(
                type = MessageType.AI_PROMPT,
                payload = prompt,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun sendTerminalInput(input: String) {
        send(
            WsMessage(
                type = MessageType.TERMINAL_INPUT,
                payload = input,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun requestFileTree(path: String = "") {
        send(
            WsMessage(
                type = MessageType.FILE_REQUEST,
                payload = path,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun sendEditorAction(action: String) {
        send(
            WsMessage(
                type = MessageType.EDITOR_ACTION,
                payload = action,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun sendBuildCommand(command: String) {
        send(
            WsMessage(
                type = MessageType.BUILD_COMMAND,
                payload = command,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun sendDevServerCommand(command: String) {
        send(
            WsMessage(
                type = MessageType.DEV_SERVER,
                payload = command,
                id = UUID.randomUUID().toString()
            )
        )
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        reconnectAttempts = maxReconnectAttempts // prevent reconnect
    }

    fun ping() {
        val start = System.currentTimeMillis()
        send(
            WsMessage(
                type = MessageType.STATUS,
                payload = "ping",
                id = UUID.randomUUID().toString()
            )
        )
        scope.launch {
            // Simplified latency measurement
            delay(50)
            if (_connectionState.value == ConnectionState.CONNECTED) {
                _latency.value = System.currentTimeMillis() - start
            }
        }
    }
}
