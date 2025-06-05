package com.bandi.textwar.domain.usecases.auth

import com.bandi.textwar.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 로그인 유스케이스
 * ViewModel에서 호출하여 로그인 로직을 담당합니다.
 * - 이메일, 비밀번호를 받아 AuthRepository에 위임합니다.
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * 실제 로그인 실행 함수
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.login(email, password)
}
