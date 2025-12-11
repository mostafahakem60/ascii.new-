package com.example.smali_translator.di

import com.example.smali_translator.data.repository.SmaliRepository
import com.example.smali_translator.data.repository.SmaliRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SmaliModule {
    @Binds
    abstract fun bindSmaliRepository(
        impl: SmaliRepositoryImpl
    ): SmaliRepository
}
