package com.cursorai.remote.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cursorai.remote.data.model.ConnectionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cursor_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val HOST_KEY = stringPreferencesKey("host")
        private val PORT_KEY = intPreferencesKey("port")
        private val USE_TLS_KEY = booleanPreferencesKey("use_tls")
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val VOICE_LANGUAGE_KEY = stringPreferencesKey("voice_language")
        private val AUTO_CONNECT_KEY = booleanPreferencesKey("auto_connect")
        private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
    }

    val connectionConfig: Flow<ConnectionConfig> = context.dataStore.data.map { prefs ->
        ConnectionConfig(
            host = prefs[HOST_KEY] ?: "192.168.1.100",
            port = prefs[PORT_KEY] ?: 9090,
            useTls = prefs[USE_TLS_KEY] ?: false,
            authToken = prefs[AUTH_TOKEN_KEY] ?: ""
        )
    }

    val voiceLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[VOICE_LANGUAGE_KEY] ?: "en-US"
    }

    val autoConnect: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_CONNECT_KEY] ?: false
    }

    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTIC_FEEDBACK_KEY] ?: true
    }

    val fontSize: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[FONT_SIZE_KEY] ?: 14
    }

    suspend fun saveConnectionConfig(config: ConnectionConfig) {
        context.dataStore.edit { prefs ->
            prefs[HOST_KEY] = config.host
            prefs[PORT_KEY] = config.port
            prefs[USE_TLS_KEY] = config.useTls
            prefs[AUTH_TOKEN_KEY] = config.authToken
        }
    }

    suspend fun saveVoiceLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[VOICE_LANGUAGE_KEY] = language
        }
    }

    suspend fun saveAutoConnect(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_CONNECT_KEY] = enabled
        }
    }

    suspend fun saveHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }

    suspend fun saveFontSize(size: Int) {
        context.dataStore.edit { prefs ->
            prefs[FONT_SIZE_KEY] = size
        }
    }
}
