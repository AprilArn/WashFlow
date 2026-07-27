package com.aprilarn.washflow.ui.aiagent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aprilarn.washflow.ai.Brain
import com.aprilarn.washflow.data.repository.CustomerRepository
import com.aprilarn.washflow.data.repository.ItemRepository
import com.aprilarn.washflow.data.repository.ServiceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.text.input.TextFieldValue
import android.content.Context
import android.speech.SpeechRecognizer

class AiAgentViewModel : ViewModel() {
    private val brain = Brain()
    private val customerRepository = CustomerRepository()
    private val itemRepository = ItemRepository()
    private val serviceRepository = ServiceRepository()
    private val _uiState = MutableStateFlow(AiAgentUiState())
    val uiState = _uiState.asStateFlow()

    private var aiFlowJob: Job? = null
    private var cooldownJob: Job? = null
    private var currentAiMessageId: String? = null

    private val _actionEvents = MutableSharedFlow<AiAgentAction>()
    val actionEvents = _actionEvents.asSharedFlow()

    private var sttManager: SpeechToTextManager? = null

    init {
        listenForCustomerChanges()
        listenForItemChanges()
        listenForServiceChanges()
    }

    override fun onCleared() {
        super.onCleared()
        sttManager?.destroy()
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

    fun onStartVoiceAgent(context: Context) {
        if (_uiState.value.voiceAgentStatus != VoiceAgentStatus.IDLE || 
            _uiState.value.isAiThinking || 
            _uiState.value.isTypewriterActive || 
            _uiState.value.modelStatus == AiModelStatus.COOLDOWN) {
            return
        }

        if (sttManager == null) {
            sttManager = SpeechToTextManager(
                context = context.applicationContext,
                onSpeechPartialResults = { partial ->
                    _uiState.update { it.copy(voiceAgentText = partial) }
                },
                onSpeechFinalResults = { final ->
                    handleFinalSpeechResult(final)
                },
                onSpeechError = { errorCode ->
                    // Guard: If we are already IDLE (dismissed), don't restart anything
                    if (_uiState.value.voiceAgentStatus != VoiceAgentStatus.IDLE) {
                        if (errorCode == SpeechRecognizer.ERROR_NO_MATCH || errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            _uiState.update { it.copy(voiceAgentStatus = VoiceAgentStatus.IDLE, voiceAgentText = "") }
                        } else {
                            // Only auto-restart if we are still in LISTENING mode
                            if (_uiState.value.voiceAgentStatus == VoiceAgentStatus.LISTENING) {
                                sttManager?.startListening()
                            }
                        }
                    }
                }
            )
        }

        _uiState.update { it.copy(
            voiceAgentStatus = VoiceAgentStatus.LISTENING,
            voiceAgentText = ""
        ) }
        sttManager?.startListening()
    }

    private fun handleFinalSpeechResult(text: String) {
        if (text.isBlank() || _uiState.value.voiceAgentStatus == VoiceAgentStatus.IDLE) {
            _uiState.update { it.copy(voiceAgentStatus = VoiceAgentStatus.IDLE) }
            return
        }

        aiFlowJob = viewModelScope.launch {
            // Step 1: Show the final transcribed text clearly for a short moment
            _uiState.update { it.copy(voiceAgentText = text) }
            kotlinx.coroutines.delay(1000) // 1000ms delay so user can read their input

            // Step 2: Add to chat history and move to thinking state
            val userMessage = ChatMessage(text = text, isUser = true)
            _uiState.update { state ->
                state.copy(
                    messages = state.messages + userMessage,
                    voiceAgentStatus = VoiceAgentStatus.THINKING,
                    voiceAgentText = "" // Clear preview as we start processing
                )
            }

            executeAiFlow(text, isFromVoice = true)
        }
    }

    fun onStopVoiceAgent() {
        _uiState.update { it.copy(voiceAgentStatus = VoiceAgentStatus.IDLE) }
        sttManager?.cancel()
        aiFlowJob?.cancel()
        aiFlowJob = null
    }

    fun onDismissVoiceOverlay() {
        _uiState.update { it.copy(voiceAgentStatus = VoiceAgentStatus.IDLE) }
    }

    fun onInputChange(newValue: TextFieldValue) {
        _uiState.update { it.copy(inputMessage = newValue) }
    }

    fun onSendMessage() {
        if (_uiState.value.modelStatus == AiModelStatus.COOLDOWN) return
        
        val currentInput = _uiState.value.inputMessage.text.trim()
        if (currentInput.isBlank()) return

        val userMessage = ChatMessage(text = currentInput, isUser = true)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                inputMessage = TextFieldValue("")
            )
        }

        aiFlowJob = viewModelScope.launch {
            executeAiFlow(currentInput, isFromVoice = false)
        }
    }

    fun onStopProcessing() {
        aiFlowJob?.cancel()
        aiFlowJob = null

        val messageIdToCleanup = currentAiMessageId
        currentAiMessageId = null

        _uiState.update { state ->
            state.copy(
                isAiThinking = false,
                isTypewriterActive = false,
                modelStatus = AiModelStatus.IDLE,
                currentModelName = null,
                voiceAgentStatus = if (state.voiceAgentStatus == VoiceAgentStatus.THINKING || 
                    state.voiceAgentStatus == VoiceAgentStatus.ANSWERING) 
                    VoiceAgentStatus.IDLE else state.voiceAgentStatus,
                messages = state.messages.filterNot { it.isThinking || it.id == messageIdToCleanup }
            )
        }
        
        messageIdToCleanup?.let { messageAnimationProgress.remove(it) }

        // Start 10-second cooldown
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update { it.copy(modelStatus = AiModelStatus.COOLDOWN) }
            for (i in 10 downTo 1) {
                _uiState.update { it.copy(currentModelName = "Cooldown ${i}s") }
                kotlinx.coroutines.delay(1000)
            }
            _uiState.update { it.copy(modelStatus = AiModelStatus.IDLE, currentModelName = null) }
        }
    }

    private suspend fun executeAiFlow(query: String, isFromVoice: Boolean) {
        _uiState.update { it.copy(isAiThinking = true) }
        if (isFromVoice) {
            _uiState.update { it.copy(voiceAgentStatus = VoiceAgentStatus.THINKING) }
        }

        // Add AI placeholder that shows the "Thinking…" state
        val aiPlaceholder = ChatMessage(text = "", isUser = false, isThinking = true)
        currentAiMessageId = aiPlaceholder.id
        
        _uiState.update { state ->
            state.copy(messages = state.messages + aiPlaceholder)
        }

        // Call the AI
        val finalResponseRaw = brain.sendMessage(query) { name, status ->
            _uiState.update { state ->
                // Guard: Hanya update status jika kita masih dalam mode Thinking
                if (state.isAiThinking) {
                    state.copy(currentModelName = name, modelStatus = status)
                } else {
                    state
                }
            }
        }
        
        // Final guard after network call
        currentCoroutineContext().ensureActive()

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

        if (isFromVoice) {
            _uiState.update { it.copy(voiceAgentStatus = VoiceAgentStatus.ANSWERING) }
        }

        // Start background typewriter simulation
        val finalAiMessageId = aiPlaceholder.id
        _uiState.update { it.copy(isTypewriterActive = true) }
        messageAnimationProgress[finalAiMessageId] = 0
        
        for (i in 1..finalResponseText.length) {
            val partialText = finalResponseText.take(i)
            messageAnimationProgress[finalAiMessageId] = i
            
            if (isFromVoice) {
                _uiState.update { it.copy(voiceAgentText = partialText) }
            }
            
            kotlinx.coroutines.delay(20) // Match UI typewriter speed
        }
        
        markMessageAsAnimated(finalAiMessageId)
        _uiState.update { it.copy(isTypewriterActive = false) }
        currentAiMessageId = null

        // GUARD: If the user swiped to dismiss during answering, do not continue voice interaction
        if (isFromVoice && _uiState.value.voiceAgentStatus == VoiceAgentStatus.IDLE) {
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(modelStatus = AiModelStatus.IDLE, currentModelName = null) }
            return
        }

        // Auto-execute immediate actions (e.g., DIRECT_NAVIGATE)
        if (parsedAction is AiAgentAction.Navigate && parsedAction.isImmediate) {
            onConfirmAction(finalAiMessageId, parsedAction)
        }

        if (isFromVoice) {
            val hasAction = parsedAction !is AiAgentAction.None &&
                    parsedAction !is AiAgentAction.Unknown
            val isImmediateNav = parsedAction is AiAgentAction.Navigate && parsedAction.isImmediate

            if (hasAction && !isImmediateNav) {
                // Perlu konfirmasi (Add/Delete/Navigate biasa)
                _uiState.update { it.copy(voiceAgentStatus = VoiceAgentStatus.WAITING_FOR_CONFIRMATION) }
            } else {
                // Navigasi Langsung (isImmediateNav) ATAU hanya ngobrol biasa (!hasAction)
                // Keduanya langsung buka mic lagi setelah jeda 3 detik
                kotlinx.coroutines.delay(3000)
                
                // FINAL GUARD: If user dismissed while waiting to restart mic
                if (_uiState.value.voiceAgentStatus == VoiceAgentStatus.IDLE) return

                _uiState.update { it.copy(
                    voiceAgentStatus = VoiceAgentStatus.LISTENING,
                    voiceAgentText = ""
                ) }
                sttManager?.startListening()
            }
        }

        kotlinx.coroutines.delay(1000) // Small buffer before resetting model status
        _uiState.update { it.copy(modelStatus = AiModelStatus.IDLE, currentModelName = null) }
    }

    fun onClearHistory() {
        brain.clearHistory()
        cooldownJob?.cancel()
        cooldownJob = null
        _uiState.update { it.copy(
            messages = emptyList(),
            isAiThinking = false,
            animatedMessageIds = emptySet(),
            modelStatus = AiModelStatus.IDLE,
            currentModelName = null
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

        val isVoiceConfirmation = _uiState.value.voiceAgentStatus == VoiceAgentStatus.WAITING_FOR_CONFIRMATION

        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(actionExecuted = true)
                } else {
                    msg
                }
            }
            state.copy(
                messages = updatedMessages,
                voiceAgentStatus = if (isVoiceConfirmation) VoiceAgentStatus.LISTENING else state.voiceAgentStatus,
                voiceAgentText = if (isVoiceConfirmation) "" else state.voiceAgentText
            )
        }

        if (isVoiceConfirmation) {
            sttManager?.startListening()
        }

        viewModelScope.launch {
            _actionEvents.emit(action)
        }
    }

    fun onCancelAction(messageId: String) {
        val isVoiceConfirmation = _uiState.value.voiceAgentStatus == VoiceAgentStatus.WAITING_FOR_CONFIRMATION

        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(actionCancelled = true)
                } else {
                    msg
                }
            }
            state.copy(
                messages = updatedMessages,
                voiceAgentStatus = if (isVoiceConfirmation) VoiceAgentStatus.LISTENING else state.voiceAgentStatus,
                voiceAgentText = if (isVoiceConfirmation) "" else state.voiceAgentText
            )
        }

        if (isVoiceConfirmation) {
            sttManager?.startListening()
        }
    }
}
