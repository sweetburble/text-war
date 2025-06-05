package com.bandi.textwar.domain.usecases.auth

import com.bandi.textwar.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 현재 세션(로그인) 상태 확인 유스케이스
 * ViewModel에서 호출하여 로그인 상태를 확인합니다.
 * - AuthRepository에 위임하여 세션 존재 여부를 반환합니다.
 */
class CheckSessionUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * 실제 세션 상태 확인 함수
     * @return true: 로그인, false: 로그아웃
     */
    suspend operator fun invoke(): Result<Boolean> =
        repository.getSessionState()
}
