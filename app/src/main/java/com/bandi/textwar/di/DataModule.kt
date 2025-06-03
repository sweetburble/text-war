package com.bandi.textwar.di

import com.bandi.textwar.data.repository.BattleRecordsRepositoryImpl
import com.bandi.textwar.data.repository.CharacterRepositoryImpl
import com.bandi.textwar.data.repository.StorageRepositoryImpl
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import com.bandi.textwar.domain.repository.CharacterRepository
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
    @Singleton // Repository는 보통 Singleton으로 관리
    abstract fun bindCharacterRepository(characterRepositoryImpl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    @Singleton
    abstract fun bindBattleRecordsRepository(battleRecordsRepositoryImpl: BattleRecordsRepositoryImpl): BattleRecordsRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(storageRepositoryImpl: StorageRepositoryImpl): StorageRepository
} 