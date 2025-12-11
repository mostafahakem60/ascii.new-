package com.example.arabic_explainer.di

import com.example.arabic_explainer.data.repository.ArabicRepository
import com.example.arabic_explainer.data.repository.ArabicRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ArabicModule {
    @Binds
    abstract fun bindArabicRepository(
        impl: ArabicRepositoryImpl
    ): ArabicRepository
}
