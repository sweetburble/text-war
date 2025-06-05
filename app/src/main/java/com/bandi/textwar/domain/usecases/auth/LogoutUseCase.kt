package com.bandi.textwar.domain.usecases.auth

import com.bandi.textwar.domain.repository.AuthRepository // 변경
import com.bandi.textwar.domain.repository.SessionRepository // 추가
import timber.log.Timber
import javax.inject.Inject

/**
 * 로그아웃 유스케이스 - 실제 구현은 Repository에 위임
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository, // 변경
    private val sessionRepository: SessionRepository // 추가
) {
    suspend operator fun invoke(): Result<Unit> {
        val logoutResult = authRepository.logout()
        if (logoutResult.isSuccess) {
            try {
                sessionRepository.clearLoginSession()
            } catch (e: Exception) {
                // 여기서는 로그아웃 자체는 성공했으므로 logoutResult를 그대로 반환하고, 세션 삭제 실패는 로깅만 한다고 가정
                Timber.e(e, "세션 삭제 실패")
            }
        }
        return logoutResult
    }
}
