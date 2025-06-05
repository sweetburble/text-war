package com.bandi.textwar.data.repository

import com.bandi.textwar.domain.repository.SettingsRepository
import com.bandi.textwar.data.datasource.AuthDataSource
import javax.inject.Inject

/**
 * SettingsRepository 구현체 - DataSource에 위임
 */
class SettingsRepositoryImpl @Inject constructor(
    private val dataSource: AuthDataSource
) : SettingsRepository {
    override suspend fun logout(): Result<Unit> = dataSource.logout()

    override suspend fun withdraw(): Result<Unit> = dataSource.withdraw()
}
