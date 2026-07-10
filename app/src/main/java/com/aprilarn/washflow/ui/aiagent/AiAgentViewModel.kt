package com.aprilarn.washflow.ui.aiagent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.ai.Brain
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.input.TextFieldValue

class AiAgentViewModel : ViewModel() {
    private val brain = Brain()
    private val customerRepository = CustomerRepository()
    private val itemRepository = ItemRepository()
    private val serviceRepository = ServiceRepository()
    private val _uiState = MutableStateFlow(AiAgentUiState())
    val uiState = _uiState.asStateFlow()

    private val _actionEvents = MutableSharedFlow<AiAgentAction>()
    val actionEvents = _actionEvents.asSharedFlow()

    init {
        listenForCustomerChanges()
        listenForItemChanges()
        listenForServiceChanges()
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

    private fun listenForItemChanges() {
        viewModelScope.launch {
            itemRepository.getItemsRealtime()
                .catch { /* Handle error */ }
                .collect { items ->
                    _uiState.update { it.copy(items = items) }
                }
        }
    }

    private fun listenForServiceChanges() {
        viewModelScope.launch {
            serviceRepository.getServicesRealtime()
                .catch { /* Handle error */ }
                .collect { services ->
                    _uiState.update { it.copy(services = services) }
                }
        }
    }

    /** Returns true if this message has already played its entry animation. */
    fun wasMessageAnimated(messageId: String): Boolean = 
        messageId in _uiState.value.animatedMessageIds

    /** Called by the panel once a message's entry animation has finished. */
    fun markMessageAsAnimated(messageId: String) {
        _uiState.update { it.copy(animatedMessageIds = it.animatedMessageIds + messageId) }
    }

    /**
     * Tracks the number of characters currently displayed for each message.
     * This ensures the typewriter animation continues even if the panel is closed.
     */
    private val messageAnimationProgress = mutableStateMapOf<String, Int>()

    fun getAnimationProgress(messageId: String): Int = messageAnimationProgress[messageId] ?: -1

    fun onToggleAiAgent() {
        _uiState.update { it.copy(expanded = !it.expanded) }
    }

    fun onDismissAiAgent() {
        _uiState.update { it.copy(expanded = false) }
    }

    fun onInputChange(newValue: TextFieldValue) {
        _uiState.update { it.copy(inputMessage = newValue) }
    }

    fun onSendMessage() {
        val currentInput = _uiState.value.inputMessage.text
        if (currentInput.isBlank()) return

        val userMessage = ChatMessage(text = currentInput, isUser = true)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                inputMessage = TextFieldValue("")
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

            // Start background typewriter simulation
            val finalAiMessageId = aiPlaceholder.id
            viewModelScope.launch {
                messageAnimationProgress[finalAiMessageId] = 0
                for (i in 1..finalResponseText.length) {
                    messageAnimationProgress[finalAiMessageId] = i
                    kotlinx.coroutines.delay(20) // Match UI typewriter speed
                }
                markMessageAsAnimated(finalAiMessageId)
            }

            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(modelStatus = AiModelStatus.IDLE, currentModelName = null) }
        }
    }

    fun onClearHistory() {
        brain.clearHistory()
        _uiState.update { it.copy(
            messages = emptyList(),
            isAiThinking = false,
            animatedMessageIds = emptySet()
        ) }
        messageAnimationProgress.clear()
        // Resetting any other internal UI states that might be tracked via callbacks
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
