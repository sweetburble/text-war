package com.bandi.textwar.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.domain.usecases.auth.CheckSessionUseCase
import com.bandi.textwar.domain.usecases.auth.LoginUseCase
import com.bandi.textwar.domain.usecases.auth.LogoutUseCase
import com.bandi.textwar.domain.usecases.auth.SignupUseCase
import com.bandi.textwar.domain.usecases.auth.WithdrawUseCase 
import com.bandi.textwar.presentation.viewmodels.state.AuthUiState
import com.bandi.textwar.presentation.viewmodels.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val checkSessionUseCase: CheckSessionUseCase,
    private val withdrawUseCase: WithdrawUseCase
) : ViewModel() {

    // 유저 닉네임 입력을 위한 MutableStateFlow
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    // 이메일 입력을 위한 MutableStateFlow
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    // 비밀번호 입력을 위한 MutableStateFlow
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // 비밀번호 확인 입력을 위한 MutableStateFlow
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // UI 상태를 나타내는 MutableStateFlow
    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // 로그인 상태를 나타내는 MutableStateFlow
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Unknown)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        checkCurrentUserSession()
    }

    /**
     * 현재 세션(로그인) 상태를 확인
     * - SupabaseClient 직접 참조 대신 CheckSessionUseCase를 호출
     * - 세션이 있으면 LoggedIn, 없으면 LoggedOut, 예외 시 Unknown 상태로 처리
     */
    private fun checkCurrentUserSession() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            when (checkSessionUseCase().getOrNull()) {
                true -> _loginState.value = LoginState.LoggedIn
                false -> _loginState.value = LoginState.LoggedOut
                null -> _loginState.value = LoginState.Unknown // 초기 또는 에러 시 Unknown
            }
            // 세션 체크 후 UI 상태는 Idle로 돌려놓거나, 로그인 상태에 따라 다른 초기 UI 상태를 설정할 수 있다.
            // 여기서는 Idle로 두어, 각 화면에서 필요에 따라 로딩을 다시 처리하도록 한다.
            if (_loginState.value != LoginState.Unknown) { // 명확한 상태가 되었을 때만 Idle로 변경
                _authUiState.value = AuthUiState.Idle
            }
        }
    }

    // 유저 닉네임 변경 함수
    fun onNicknameChange(nickname: String) {
        _nickname.value = nickname
    }

    // 이메일 변경 함수
    fun onEmailChange(email: String) {
        _email.value = email
    }

    // 비밀번호 변경 함수
    fun onPasswordChange(password: String) {
        _password.value = password
    }

    // 비밀번호 확인 변경 함수
    fun onConfirmPasswordChange(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
    }

    /**
     * 로그인 함수 - LoginUseCase를 호출하여 인증을 처리
     * 비동기로 실행하며, 결과에 따라 UI 상태와 로그인 상태를 갱신
     */
    fun loginUser() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = loginUseCase(_email.value, _password.value)
            if (result.isSuccess) {
                Timber.d("로그인에 성공하였습니다!")
                _authUiState.value = AuthUiState.Success("로그인 성공!")
                _loginState.value = LoginState.LoggedIn
            } else {
                val errorMessage = when (result.exceptionOrNull()?.message) {
                    null, "" -> "로그인 중 알 수 없는 에러가 발생했습니다."
                    "Invalid login credentials" -> "이메일 또는 비밀번호가 올바르지 않습니다."
                    else -> result.exceptionOrNull()?.message
                }
                _authUiState.value = AuthUiState.Error(errorMessage ?: "로그인 에러")
                _loginState.value = LoginState.LoggedOut // 로그인 실패 시에도 LoggedOut 상태 유지
            }
        }
    }

    /**
     * 회원가입 함수 - SignupUseCase를 호출하여 인증을 처리
     * 비밀번호 확인이 일치하지 않을 경우 에러 상태를 반환
     */
    fun signUpUser() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            if (_password.value != _confirmPassword.value) {
                _authUiState.value = AuthUiState.Error("비밀번호가 일치하지 않습니다.")
                return@launch
            }
            val result = signupUseCase(_email.value, _password.value, _nickname.value)
            if (result.isSuccess) {
                _authUiState.value = AuthUiState.Success("회원가입 성공! 확인 이메일을 확인해주세요.")
                // 회원가입 성공 후 바로 로그인 상태로 만들지 않고, 이메일 인증 등을 기다릴 수 있으므로
                // _loginState는 변경하지 않거나, 필요에 따라 LoggedOut 상태로 명시할 수 있다.
                // 현재는 변경하지 않음.
            } else {
                val errorMessage = when (result.exceptionOrNull()?.message) {
                    null, "" -> "회원가입 중 알 수 없는 에러가 발생했습니다."
                    "User already registered" -> "이미 가입된 이메일입니다."
                    else -> result.exceptionOrNull()?.message
                }
                _authUiState.value = AuthUiState.Error(errorMessage ?: "회원가입 에러")
            }
        }
    }

    /**
     * 로그아웃 함수 - LogoutUseCase를 호출하여 인증을 처리
     * 성공 시 상태 및 입력값 초기화, 실패 시 에러 상태로 처리
     */
    fun logoutUser() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading // 로그아웃 시작 시 로딩 상태
            val result = logoutUseCase()
            if (result.isSuccess) {
                _loginState.value = LoginState.LoggedOut
                _email.value = ""
                _password.value = ""
                _confirmPassword.value = ""
                _nickname.value = "" // 닉네임도 초기화
                _authUiState.value = AuthUiState.Idle // 메시지는 토스트로 처리
            } else {
                // 로그아웃 실패 시에도 UI에 에러를 표시하고, 로그인 상태는 그대로 유지될 수 있음 (서버 문제 등)
                // 또는 강제로 LoggedOut으로 변경할 수도 있으나, 여기서는 에러만 표시
                _authUiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "로그아웃 중 에러가 발생했습니다.")
            }
        }
    }

    /**
     * 회원탈퇴 함수 - WithdrawUseCase를 호출하여 처리
     * 성공 시 상태 및 입력값 초기화, 실패 시 에러 상태로 처리
     */
    fun withdrawUser() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading // 회원탈퇴 시작 시 로딩 상태
            val result = withdrawUseCase()
            if (result.isSuccess) {
                _loginState.value = LoginState.LoggedOut // 회원탈퇴 성공 시 로그아웃 상태로 변경
                _email.value = ""
                _password.value = ""
                _confirmPassword.value = ""
                _nickname.value = ""
                _authUiState.value = AuthUiState.Idle // 메시지는 토스트로 처리
            } else {
                _authUiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "회원탈퇴 중 에러가 발생했습니다.")
                // 회원탈퇴 실패 시 로그인 상태는 유지될 수 있음
            }
        }
    }

    // UI 상태 초기화 함수
    fun resetAuthUiState() {
        _authUiState.value = AuthUiState.Idle
    }
}