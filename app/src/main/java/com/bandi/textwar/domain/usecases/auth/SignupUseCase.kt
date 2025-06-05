package com.bandi.textwar.domain.usecases.auth

import com.bandi.textwar.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 회원가입 유스케이스
 * ViewModel에서 호출하여 회원가입 로직을 담당합니다.
 * - 이메일, 비밀번호, 닉네임을 받아 AuthRepository에 위임합니다.
 */
class SignupUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * 실제 회원가입 실행 함수
     */
    suspend operator fun invoke(email: String, password: String, nickname: String): Result<Unit> =
        repository.signup(email, password, nickname)
}
