package com.aprilarn.washflow.ui.aiagent

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
        // Mock sending message logic
        _uiState.update { it.copy(inputMessage = "") }
    }
}
