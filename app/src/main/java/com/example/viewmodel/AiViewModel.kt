package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiRepository
import com.example.data.ChatMessage
import com.example.data.ConceptImageSize
import com.example.data.GeneratedConceptResult
import com.example.data.PlaceholderAiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface ImageStudioUiState {
    data object Idle : ImageStudioUiState
    data class Loading(val size: ConceptImageSize) : ImageStudioUiState
    data class Success(val result: GeneratedConceptResult) : ImageStudioUiState
    data class Error(val message: String) : ImageStudioUiState
}

data class AdvisorChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

class AiViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository: AiRepository = PlaceholderAiRepository(application.applicationContext)

    val isFirebaseEnabled: Boolean
        get() = aiRepository.isFirebaseEnabled

    val systemInstruction: String
        get() = aiRepository.systemInstruction

    private val _chatState = MutableStateFlow(
        AdvisorChatUiState(
            messages = listOf(
                ChatMessage(
                    id = "welcome",
                    isUser = false,
                    text = "Welcome, Frontier Pioneer! I'm your Tycoon Advisor. Ask me what to upgrade next, which biome to unlock, or how factories multiply your resource income.",
                    isSampleNotice = !aiRepository.isFirebaseEnabled
                )
            )
        )
    )
    val chatState: StateFlow<AdvisorChatUiState> = _chatState.asStateFlow()

    private val _selectedImageSize = MutableStateFlow(ConceptImageSize.SIZE_1K)
    val selectedImageSize: StateFlow<ConceptImageSize> = _selectedImageSize.asStateFlow()

    private val _studioPrompt = MutableStateFlow(
        "Low-poly stylized 3D frontier sawmill and crystal mine with warm sunset shadows"
    )
    val studioPrompt: StateFlow<String> = _studioPrompt.asStateFlow()

    private val _studioState = MutableStateFlow<ImageStudioUiState>(ImageStudioUiState.Idle)
    val studioState: StateFlow<ImageStudioUiState> = _studioState.asStateFlow()

    fun sendAdvisorMessage(rawInput: String, gameContextSummary: String) {
        val text = rawInput.trim()
        if (text.isEmpty() || _chatState.value.isSending) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = text
        )
        val historyBefore = _chatState.value.messages
        _chatState.value = _chatState.value.copy(
            messages = historyBefore + userMsg,
            isSending = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val result = aiRepository.sendAdvisorMessage(
                    history = historyBefore,
                    userMessage = text,
                    gameContextSummary = gameContextSummary
                )
                result.onSuccess { reply ->
                    val modelMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        isUser = false,
                        text = reply,
                        isSampleNotice = !aiRepository.isFirebaseEnabled
                    )
                    _chatState.value = _chatState.value.copy(
                        messages = _chatState.value.messages + modelMsg,
                        isSending = false
                    )
                }.onFailure { err ->
                    _chatState.value = _chatState.value.copy(
                        isSending = false,
                        errorMessage = err.localizedMessage ?: "Advisor request failed."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    isSending = false,
                    errorMessage = e.localizedMessage ?: "Advisor request failed."
                )
            }
        }
    }

    fun clearChat() {
        _chatState.value = AdvisorChatUiState(
            messages = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "Chat cleared. Ask anything about upgrades, factories, or biome expansion!",
                    isSampleNotice = !aiRepository.isFirebaseEnabled
                )
            )
        )
    }

    fun setSelectedImageSize(size: ConceptImageSize) {
        _selectedImageSize.value = size
    }

    fun setStudioPrompt(prompt: String) {
        _studioPrompt.value = prompt
    }

    fun generateConceptImage() {
        val prompt = _studioPrompt.value.trim()
        if (prompt.isEmpty()) {
            _studioState.value = ImageStudioUiState.Error("Please enter a prompt to generate artwork.")
            return
        }
        val targetSize = _selectedImageSize.value
        _studioState.value = ImageStudioUiState.Loading(targetSize)

        viewModelScope.launch {
            try {
                val result = aiRepository.generateConceptImage(prompt, targetSize)
                result.onSuccess { generated ->
                    _studioState.value = ImageStudioUiState.Success(generated)
                }.onFailure { err ->
                    _studioState.value = ImageStudioUiState.Error(
                        err.localizedMessage ?: "Image generation failed."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _studioState.value = ImageStudioUiState.Error(
                    e.localizedMessage ?: "Image generation failed."
                )
            }
        }
    }
}
