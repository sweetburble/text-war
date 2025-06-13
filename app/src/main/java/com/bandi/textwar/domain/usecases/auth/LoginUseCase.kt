package com.bandi.textwar.domain.usecases.auth

import com.bandi.textwar.domain.repository.AuthRepository
import com.bandi.textwar.domain.repository.SessionRepository // 추가
import timber.log.Timber
import javax.inject.Inject

/**
 * 로그인 유스케이스
 * ViewModel에서 호출하여 로그인 로직을 담당
 * - 이메일, 비밀번호를 받아 AuthRepository에 위임
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository, // 이름 변경 (AuthRepository)
    private val sessionRepository: SessionRepository // 추가
) {
    /**
     * 실제 로그인 실행 함수
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        val loginResult = authRepository.login(email, password)
        if (loginResult.isSuccess) {
            try {
                sessionRepository.saveLoginSession()
            } catch (e: Exception) {
                // 여기서는 로그인 자체는 성공했으므로 loginResult를 그대로 반환하고, 세션 저장 실패는 로깅만 한다고 가정
                Timber.e(e, "세션 저장 실패")
            }
        }
        return loginResult
    }
}
