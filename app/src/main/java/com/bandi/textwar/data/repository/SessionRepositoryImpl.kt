package com.bandi.textwar.data.repository

import com.bandi.textwar.data.datasource.local.SessionLocalDataSource
import com.bandi.textwar.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * SessionRepository의 구현체
 * SessionLocalDataSource를 통해 로컬 세션 데이터를 관리
 */
class SessionRepositoryImpl @Inject constructor(
    private val localDataSource: SessionLocalDataSource
) : SessionRepository {

    /**
     * 현재 로그인 상태를 반환
     *
     * @return 로그인 되어있으면 true, 아니면 false
     */
    override suspend fun isLoggedIn(): Boolean {
        return localDataSource.isLoggedIn()
    }

    /**
     * 로그인 상태를 저장
     */
    override suspend fun saveLoginSession() {
        localDataSource.saveLoginSession()
    }

    /**
     * 저장된 로그인 세션을 삭제
     */
    override suspend fun clearLoginSession() {
        localDataSource.clearLoginSession()
    }
}
