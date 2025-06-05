package com.bandi.textwar.di

import com.bandi.textwar.data.datasource.remote.OpenAIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideOpenAIService(): OpenAIService {
        return OpenAIService()
    }
} 