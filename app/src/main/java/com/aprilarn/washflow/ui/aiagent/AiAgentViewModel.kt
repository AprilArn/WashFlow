package com.aprilarn.washflow.ui.aiagent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.ai.Brain
import com.aprilarn.washflow.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiAgentViewModel : ViewModel() {
    private val brain = Brain()
    private val customerRepository = CustomerRepository()
    private val _uiState = MutableStateFlow(AiAgentUiState())
    val uiState = _uiState.asStateFlow()

    private val _actionEvents = MutableSharedFlow<AiAgentAction>()
    val actionEvents = _actionEvents.asSharedFlow()

    init {
        listenForCustomerChanges()
    }

    private fun listenForCustomerChanges() {
        viewModelScope.launch {
            customerRepository.getCustomersRealtime()
                .catch { /* Handle error if needed */ }
                .collect { customers ->
                    _uiState.update { it.copy(customers = customers) }
                }
        }
    }

    /**
     * Stores IDs of messages whose entry animation has already played.
     * Lives in the ViewModel so it survives panel close/reopen (AnimatedVisibility
     * removes composables from the tree when hidden, destroying all remember{} state).
     * Only cleared when the user explicitly deletes chat history.
     */
    private val animatedMessageIds = HashSet<String>()

    /** Returns true if this message has already played its entry animation. */
    fun wasMessageAnimated(messageId: String): Boolean = messageId in animatedMessageIds

    /** Called by the panel once a message's entry animation has finished. */
    fun markMessageAsAnimated(messageId: String) {
        animatedMessageIds.add(messageId)
    }

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

        viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            // Add AI placeholder that shows the "Thinking…" state
            val aiPlaceholder = ChatMessage(text = "", isUser = false, isThinking = true)
            _uiState.update { state ->
                state.copy(messages = state.messages + aiPlaceholder)
            }

            // Call the AI
            val finalResponseRaw = brain.sendMessage(currentInput) { name, status ->
                _uiState.update { it.copy(currentModelName = name, modelStatus = status) }
            }

            val parsedAction = AiAgentParser.parseAction(finalResponseRaw) ?: AiAgentAction.None
            val finalResponseText = AiAgentParser.cleanText(finalResponseRaw)

            // Replace placeholder with the final response
            _uiState.update { state ->
                val updatedMessages = state.messages.map { msg ->
                    if (msg.id == aiPlaceholder.id) {
                        msg.copy(
                            text = finalResponseText,
                            isThinking = false,
                            action = parsedAction
                        )
                    } else {
                        msg
                    }
                }
                state.copy(
                    messages = updatedMessages,
                    modelStatus = if (finalResponseText.startsWith("Maaf, semua layanan"))
                        AiModelStatus.FAILURE
                    else
                        AiModelStatus.SUCCESS,
                    isAiThinking = false
                )
            }

            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(modelStatus = AiModelStatus.IDLE, currentModelName = null) }
        }
    }

    fun onClearHistory() {
        brain.clearHistory()
        // Also clear animation tracking so messages animate again if re-added
        animatedMessageIds.clear()
        _uiState.update { it.copy(messages = emptyList(), isAiThinking = false) }
    }

    fun setUserInfo(name: String, photoUrl: String?) {
        _uiState.update { it.copy(userName = name, profilePictureUrl = photoUrl) }
    }

    fun onConfirmAction(messageId: String, updatedAction: AiAgentAction? = null) {
        val message = _uiState.value.messages.find { it.id == messageId } ?: return
        val action = updatedAction ?: message.action ?: return

        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(actionExecuted = true)
                } else {
                    msg
                }
            }
            state.copy(messages = updatedMessages)
        }

        viewModelScope.launch {
            _actionEvents.emit(action)
        }
    }

    fun onCancelAction(messageId: String) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(actionCancelled = true)
                } else {
                    msg
                }
            }
            state.copy(messages = updatedMessages)
        }
    }
}
