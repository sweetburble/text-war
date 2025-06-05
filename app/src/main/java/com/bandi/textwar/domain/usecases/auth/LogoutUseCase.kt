package com.bandi.textwar.domain.usecases.auth

import com.bandi.textwar.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 로그아웃 유스케이스 - 실제 구현은 Repository에 위임
 */
class LogoutUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.logout()
}
