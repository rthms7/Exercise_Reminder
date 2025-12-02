package com.example.exercise_reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessageViewModel(private val context: Context) : ViewModel() {

    val messages = MessageRepository.getMessages(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val history = MessageRepository.getMessageHistory(context)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addMessage(msg: String) {
        viewModelScope.launch {
            val updated = messages.value + msg
            MessageRepository.saveMessages(context, updated)
            MessageRepository.addToHistory(context, msg)
        }
    }

    fun deleteMessage(msg: String) {
        viewModelScope.launch {
            val updated = messages.value - msg
            MessageRepository.saveMessages(context, updated)
        }
    }

    fun editMessage(old: String, new: String) {
        viewModelScope.launch {
            val updated = messages.value.map { if (it == old) new else it }
            MessageRepository.saveMessages(context, updated)
            MessageRepository.addToHistory(context, new)
        }
    }
}
