package com.bandi.textwar.di

import com.bandi.textwar.data.datasource.local.SessionLocalDataSource // 추가
import com.bandi.textwar.data.datasource.local.SessionLocalDataSourceImpl // 추가
import com.bandi.textwar.data.repository.AuthRepositoryImpl
import com.bandi.textwar.data.repository.BattleRecordsRepositoryImpl
import com.bandi.textwar.data.repository.CharacterRepositoryImpl
import com.bandi.textwar.data.repository.SessionRepositoryImpl // 추가
import com.bandi.textwar.data.repository.SettingsRepositoryImpl
import com.bandi.textwar.data.repository.StorageRepositoryImpl
import com.bandi.textwar.domain.repository.AuthRepository
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import com.bandi.textwar.domain.repository.CharacterRepository
import com.bandi.textwar.domain.repository.SessionRepository // 추가
import com.bandi.textwar.domain.repository.SettingsRepository
import com.bandi.textwar.domain.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(characterRepositoryImpl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    @Singleton
    abstract fun bindBattleRecordsRepository(battleRecordsRepositoryImpl: BattleRecordsRepositoryImpl): BattleRecordsRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(storageRepositoryImpl: StorageRepositoryImpl): StorageRepository

    // SettingsRepository DI 바인딩
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    // Session 관련 바인딩 추가
    @Binds
    @Singleton
    abstract fun bindSessionLocalDataSource(sessionLocalDataSourceImpl: SessionLocalDataSourceImpl): SessionLocalDataSource

    @Binds
    @Singleton
    abstract fun bindSessionRepository(sessionRepositoryImpl: SessionRepositoryImpl): SessionRepository
}