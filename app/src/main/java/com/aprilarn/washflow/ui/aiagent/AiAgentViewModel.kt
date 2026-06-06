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

        // Jalankan proses AI setelah animasi bubble user selesai
        viewModelScope.launch {
            // Tunggu animasi bubble user selesai (sekitar 500-600ms)
            kotlinx.coroutines.delay(600) 
            
            _uiState.update { it.copy(isAiThinking = true) }
            
            // Simulasi proses berpikir AI
            kotlinx.coroutines.delay(1500)
            
            val aiResponse = ChatMessage(text = "You asked: $currentInput", isUser = false)
            
            _uiState.update { state ->
                state.copy(
                    messages = state.messages + aiResponse,
                    isAiThinking = false
                )
            }
        }
    }

    fun setUserInfo(name: String, photoUrl: String?) {
        _uiState.update { it.copy(userName = name, profilePictureUrl = photoUrl) }
    }
}
