package com.bandi.textwar.domain.repository

/**
 * 인증 관련 도메인 인터페이스
 * ViewModel, UseCase에서 인증 기능을 추상화하여 사용합니다.
 * - 구현체는 data/repository/AuthRepositoryImpl에서 작성합니다.
 */
interface AuthRepository {
    /**
     * 로그인
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     */
    suspend fun login(email: String, password: String): Result<Unit>

    /**
     * 회원가입
     * @param email 사용자 이메일
     * @param password 사용자 비밀번호
     * @param nickname 닉네임
     */
    suspend fun signup(email: String, password: String, nickname: String): Result<Unit>

    /**
     * 로그아웃
     */
    suspend fun logout(): Result<Unit>

    /**
     * 회원탈퇴
     */
    suspend fun withdraw(): Result<Unit>

    /**
     * 현재 세션(로그인) 상태 조회
     * @return true: 로그인, false: 로그아웃
     */
    suspend fun getSessionState(): Result<Boolean>
}
