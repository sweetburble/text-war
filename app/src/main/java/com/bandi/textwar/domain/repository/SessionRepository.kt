package com.bandi.textwar.domain.repository

/**
 * 세션 정보(로그인 상태)를 관리하는 저장소 인터페이스
 */
interface SessionRepository {
    /**
     * 현재 로그인 상태를 반환
     *
     * @return 로그인 되어있으면 true, 아니면 false
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * 로그인 상태를 저장
     */
    suspend fun saveLoginSession()

    /**
     * 저장된 로그인 세션을 삭제
     */
    suspend fun clearLoginSession()
}
