package com.smali.translator.ui.batch

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smali.translator.domain.model.BatchProgress
import com.smali.translator.domain.usecase.BatchTranslateUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BatchViewModel(
    private val batchTranslateUseCase: BatchTranslateUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<BatchUiState>(BatchUiState.Idle)
    val uiState: LiveData<BatchUiState> = _uiState

    private val _progress = MutableLiveData<BatchProgress?>()
    val progress: LiveData<BatchProgress?> = _progress

    private var currentJob: Job? = null
    private var currentJobId: Long = 0

    fun startBatchTranslation(uris: List<Uri>) {
        if (uris.isEmpty()) {
            _uiState.value = BatchUiState.Error("No files selected")
            return
        }

        currentJob?.cancel()
        
        viewModelScope.launch {
            _uiState.value = BatchUiState.Preparing
            
            try {
                currentJobId = batchTranslateUseCase.createBatchJob(uris)
                _uiState.value = BatchUiState.Processing
                
                currentJob = launch {
                    batchTranslateUseCase.executeBatch(currentJobId, uris)
                        .collect { progress ->
                            _progress.value = progress
                            
                            if (progress.currentIndex >= progress.totalFiles) {
                                _uiState.value = BatchUiState.Completed(
                                    totalFiles = progress.totalFiles,
                                    completed = progress.completedCount,
                                    failed = progress.failedCount
                                )
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.value = BatchUiState.Error(e.message ?: "Batch processing failed")
            }
        }
    }

    fun cancelBatchJob() {
        viewModelScope.launch {
            currentJob?.cancel()
            if (currentJobId > 0) {
                batchTranslateUseCase.cancelBatchJob(currentJobId)
                _uiState.value = BatchUiState.Cancelled
            }
        }
    }

    fun resetState() {
        _uiState.value = BatchUiState.Idle
        _progress.value = null
        currentJobId = 0
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}

sealed class BatchUiState {
    object Idle : BatchUiState()
    object Preparing : BatchUiState()
    object Processing : BatchUiState()
    data class Completed(
        val totalFiles: Int,
        val completed: Int,
        val failed: Int
    ) : BatchUiState()
    object Cancelled : BatchUiState()
    data class Error(val message: String) : BatchUiState()
}
