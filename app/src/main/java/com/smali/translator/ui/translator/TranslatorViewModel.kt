package com.smali.translator.ui.translator

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smali.translator.domain.model.TranslationResult
import com.smali.translator.domain.model.ValidationError
import com.smali.translator.domain.usecase.ExportResultUseCase
import com.smali.translator.domain.usecase.TranslateFileUseCase
import kotlinx.coroutines.launch

class TranslatorViewModel(
    private val translateFileUseCase: TranslateFileUseCase,
    private val exportResultUseCase: ExportResultUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<TranslatorUiState>(TranslatorUiState.Idle)
    val uiState: LiveData<TranslatorUiState> = _uiState

    private val _currentTranslation = MutableLiveData<TranslationResult?>()
    val currentTranslation: LiveData<TranslationResult?> = _currentTranslation

    fun translateFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = TranslatorUiState.Loading
            
            when (val result = translateFileUseCase.execute(uri)) {
                is TranslateFileUseCase.Result.Success -> {
                    _currentTranslation.value = result.translation
                    _uiState.value = TranslatorUiState.Success(result.translation)
                }
                is TranslateFileUseCase.Result.ValidationError -> {
                    _uiState.value = TranslatorUiState.ValidationError(result.error)
                }
                is TranslateFileUseCase.Result.IOError -> {
                    _uiState.value = TranslatorUiState.Error(result.message)
                }
                is TranslateFileUseCase.Result.TranslationError -> {
                    _uiState.value = TranslatorUiState.Error(result.message)
                }
            }
        }
    }

    fun exportToFile(uri: Uri, includeExplanation: Boolean) {
        val translation = _currentTranslation.value ?: return
        
        viewModelScope.launch {
            _uiState.value = TranslatorUiState.Exporting
            
            when (val result = exportResultUseCase.exportToFile(uri, translation, includeExplanation)) {
                is ExportResultUseCase.ExportResult.Success -> {
                    _uiState.value = TranslatorUiState.ExportSuccess(result.fileName)
                }
                is ExportResultUseCase.ExportResult.Error -> {
                    _uiState.value = TranslatorUiState.Error(result.message)
                }
            }
        }
    }

    fun createShareIntent(includeExplanation: Boolean) {
        val translation = _currentTranslation.value ?: return
        val intent = exportResultUseCase.createShareIntent(translation, includeExplanation)
        _uiState.value = TranslatorUiState.ShareReady(intent)
    }

    fun resetState() {
        _uiState.value = TranslatorUiState.Idle
    }
}

sealed class TranslatorUiState {
    object Idle : TranslatorUiState()
    object Loading : TranslatorUiState()
    data class Success(val translation: TranslationResult) : TranslatorUiState()
    data class ValidationError(val error: ValidationError) : TranslatorUiState()
    data class Error(val message: String) : TranslatorUiState()
    object Exporting : TranslatorUiState()
    data class ExportSuccess(val fileName: String) : TranslatorUiState()
    data class ShareReady(val intent: android.content.Intent) : TranslatorUiState()
}
