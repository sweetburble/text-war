package com.bandi.textwar.data.repository

import com.bandi.textwar.domain.repository.AuthRepository
import com.bandi.textwar.data.datasource.AuthDataSource
import javax.inject.Inject

/**
 * 인증 도메인 인터페이스 구현체
 * 실제 인증 처리는 AuthDataSource에 위임합니다.
 * - Hilt로 주입받아 사용합니다.
 */
class AuthRepositoryImpl @Inject constructor(
    private val dataSource: AuthDataSource
): AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> =
        dataSource.login(email, password)

    override suspend fun signup(email: String, password: String, nickname: String): Result<Unit> =
        dataSource.signup(email, password, nickname)

    override suspend fun logout(): Result<Unit> =
        dataSource.logout()

    override suspend fun withdraw(): Result<Unit> =
        dataSource.withdraw()

    override suspend fun getSessionState(): Result<Boolean> =
        dataSource.getSessionState()
}
