package com.example.shared_models

import kotlinx.serialization.Serializable

@Serializable
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error<T>(val exception: String) : Result<T>()
    class Loading<T> : Result<T>()
}
