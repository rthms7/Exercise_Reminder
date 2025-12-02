package com.example.exercise_reminder

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey

private val Context.dataStore by preferencesDataStore("app_prefs")

object MessageRepository {

    private val MESSAGES: Preferences.Key<Set<String>> =
        stringSetPreferencesKey("messages")

    private val MESSAGE_HISTORY: Preferences.Key<Set<String>> =
        stringSetPreferencesKey("message_history")

    private fun limitHistory(set: Set<String>, max: Int = 100): Set<String> {
        return set.toList().takeLast(max).toSet()   // keep newest 100
    }

    fun getMessages(context: Context): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[MESSAGES]?.toList() ?: emptyList()
        }

    fun getMessageHistory(context: Context): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            prefs[MESSAGE_HISTORY]?.toList() ?: emptyList()
        }

    suspend fun saveMessages(context: Context, list: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[MESSAGES] = list.toSet()

            val history = prefs[MESSAGE_HISTORY] ?: emptySet()
            val updated = (history + list.toSet()).toList().takeLast(100).toSet()
            prefs[MESSAGE_HISTORY] = updated
        }
    }

    suspend fun addToHistory(context: Context, msg: String) {
        context.dataStore.edit { prefs ->
            val history = prefs[MESSAGE_HISTORY] ?: emptySet()
            val updated = (history + msg).toList().takeLast(100).toSet()
            prefs[MESSAGE_HISTORY] = updated
        }
    }
}
