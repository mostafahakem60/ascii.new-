package com.example.smali_translator.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smali_translator.domain.usecase.TranslateSmaliUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmaliViewModel @Inject constructor(
    private val translateSmaliUseCase: TranslateSmaliUseCase
) : ViewModel() {

    private val _translationResult = MutableStateFlow<String>("")
    val translationResult: StateFlow<String> = _translationResult

    fun translateCode(code: String) {
        viewModelScope.launch {
            val result = translateSmaliUseCase(code)
            _translationResult.value = result
        }
    }
}
