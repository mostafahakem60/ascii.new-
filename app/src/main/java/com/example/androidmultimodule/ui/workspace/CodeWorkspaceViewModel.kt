package com.example.androidmultimodule.ui.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arabic_explainer.data.model.ArabicWord
import com.example.arabic_explainer.domain.usecase.ExplainArabicWordUseCase
import com.example.smali_translator.domain.usecase.TranslateSmaliUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CodeWorkspaceUiState(
    val smaliCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val javaTranslation: String? = null,
    val explainedTerms: List<ArabicWord> = emptyList()
)

@HiltViewModel
class CodeWorkspaceViewModel @Inject constructor(
    private val translateSmaliUseCase: TranslateSmaliUseCase,
    private val explainArabicWordUseCase: ExplainArabicWordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodeWorkspaceUiState())
    val uiState: StateFlow<CodeWorkspaceUiState> = _uiState.asStateFlow()

    fun onCodeChange(newCode: String) {
        _uiState.update { it.copy(smaliCode = newCode, error = null) }
    }

    fun analyzeCode() {
        val code = _uiState.value.smaliCode
        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Please enter some Smali code first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, javaTranslation = null, explainedTerms = emptyList()) }
            try {
                val translation = translateSmaliUseCase(code)
                
                // Simple tokenizer: split by whitespace to find terms to explain
                val tokens = code.split("\\s+".toRegex())
                    .filter { it.length > 2 } // filter very short words
                    .distinct()
                    .take(5) // Limit to 5 terms for explanation demo
                
                val explanations = tokens.mapNotNull { token ->
                    explainArabicWordUseCase(token)
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        javaTranslation = translation,
                        explainedTerms = explanations
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }
}
