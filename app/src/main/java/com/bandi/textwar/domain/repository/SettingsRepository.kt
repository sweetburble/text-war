package com.bandi.textwar.domain.repository

/**
 * Settings 관련 기능 추상화(로그아웃, 회원탈퇴 등)
 */
interface SettingsRepository {
    /**
     * 로그아웃
     */
    suspend fun logout(): Result<Unit>

    /**
     * 회원탈퇴
     */
    suspend fun withdraw(): Result<Unit>
}
