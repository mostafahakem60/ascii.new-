package com.example.smali_translator.domain.usecase

import com.example.smali_translator.data.repository.SmaliRepository
import javax.inject.Inject

class TranslateSmaliUseCase @Inject constructor(
    private val repository: SmaliRepository
) {
    suspend operator fun invoke(code: String): String {
        return repository.translateSmali(code)
    }
}
