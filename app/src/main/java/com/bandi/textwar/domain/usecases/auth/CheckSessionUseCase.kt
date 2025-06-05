package com.bandi.textwar.domain.usecases.auth

import com.bandi.textwar.domain.repository.SessionRepository // 변경
import timber.log.Timber
import javax.inject.Inject

/**
 * 현재 세션(로그인) 상태 확인 유스케이스
 * ViewModel에서 호출하여 로그인 상태를 확인합니다.
 * - AuthRepository에 위임하여 세션 존재 여부를 반환합니다.
 */
class CheckSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository // 변경
) {
    /**
     * 실제 세션 상태 확인 함수
     * @return true: 로그인, false: 로그아웃
     */
    suspend operator fun invoke(): Result<Boolean> {
        // SessionRepository의 isLoggedIn은 Boolean을 직접 반환하므로 Result로 감싸줍니다.
        // 예외 발생 가능성이 있다면 try-catch로 감싸고 Result.failure를 반환할 수 있습니다.
        // 여기서는 SessionRepository가 예외를 내부적으로 처리한다고 가정합니다.
        return try {
            Result.success(sessionRepository.isLoggedIn())
        } catch (e: Exception) {
            // 필요하다면 Timber 등으로 로깅
            Timber.e(e.toString())
            Result.failure(e) // 또는 기본값 false로 처리: Result.success(false)
        }
    }
}
