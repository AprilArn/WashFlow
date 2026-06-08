package com.aprilarn.washflow.ui.aiagent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiAgentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AiAgentUiState())
    val uiState = _uiState.asStateFlow()

    fun onToggleAiAgent() {
        _uiState.update { it.copy(expanded = !it.expanded) }
    }

    fun onDismissAiAgent() {
        _uiState.update { it.copy(expanded = false) }
    }

    fun onInputChange(newValue: String) {
        _uiState.update { it.copy(inputMessage = newValue) }
    }

    fun onSendMessage() {
        val currentInput = _uiState.value.inputMessage
        if (currentInput.isBlank()) return

        val userMessage = ChatMessage(text = currentInput, isUser = true)
        
        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                inputMessage = ""
            )
        }

        // Jalankan proses AI
        viewModelScope.launch {
            // Tunggu sebentar agar animasi bubble user muncul
            kotlinx.coroutines.delay(600) 
            
            // Tambahkan placeholder AI yang sedang "berpikir"
            val aiPlaceholder = ChatMessage(text = "", isUser = false, isThinking = true)
            _uiState.update { state ->
                state.copy(messages = state.messages + aiPlaceholder)
            }
            
            // Simulasi proses berpikir AI
            kotlinx.coroutines.delay(1500)
            
            val finalResponseText = "You asked: $currentInput"
            
            // UPDATE placeholder tadi menjadi response final
            _uiState.update { state ->
                val updatedMessages = state.messages.map { msg ->
                    if (msg.id == aiPlaceholder.id) {
                        msg.copy(text = finalResponseText, isThinking = false)
                    } else {
                        msg
                    }
                }
                state.copy(messages = updatedMessages)
            }
        }
    }

    fun onClearHistory() {
        _uiState.update { it.copy(messages = emptyList(), isAiThinking = false) }
    }

    fun setUserInfo(name: String, photoUrl: String?) {
        _uiState.update { it.copy(userName = name, profilePictureUrl = photoUrl) }
    }
}
