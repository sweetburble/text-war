package com.bandi.textwar.data.datasource.local

/**
 * 세션 관련 데이터를 로컬에 저장하고 불러오는 데이터 소스 인터페이스입니다.
 */
interface SessionLocalDataSource {
    /**
     * 현재 로그인 상태를 반환합니다.
     *
     * @return 로그인 되어있으면 true, 아니면 false
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * 로그인 상태를 저장합니다.
     */
    suspend fun saveLoginSession()

    /**
     * 저장된 로그인 세션을 삭제합니다.
     */
    suspend fun clearLoginSession()
}
