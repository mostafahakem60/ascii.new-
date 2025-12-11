package com.example.arabic_explainer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arabic_explainer.data.model.ArabicWord
import com.example.arabic_explainer.domain.usecase.ExplainArabicWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArabicViewModel @Inject constructor(
    private val explainArabicWordUseCase: ExplainArabicWordUseCase
) : ViewModel() {

    private val _arabicWord = MutableStateFlow<ArabicWord?>(null)
    val arabicWord: StateFlow<ArabicWord?> = _arabicWord

    fun explainWord(word: String) {
        viewModelScope.launch {
            val result = explainArabicWordUseCase(word)
            _arabicWord.value = result
        }
    }
}
