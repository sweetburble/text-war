package com.bandi.textwar.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

// 인증 상태를 나타내는 sealed interface
sealed interface AuthUiState {
    object Idle : AuthUiState // 초기 상태
    object Loading : AuthUiState // 로딩 중 상태
    data class Success(val message: String) : AuthUiState // 성공 상태
    data class Error(val message: String) : AuthUiState // 에러 상태
}

// 로그인 상태를 나타내는 sealed interface
sealed interface LoginState {
    object Unknown : LoginState      // 초기 알 수 없는 상태 또는 확인 중
    object LoggedIn : LoginState     // 로그인 된 상태
    object LoggedOut : LoginState    // 로그아웃 된 상태
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
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

    // 현재 사용자 세션 확인 함수
    private fun checkCurrentUserSession() {
        viewModelScope.launch {
             // 세션 상태 변경을 지속적으로 감지하려면 sessionStatus Flow를 collect 할 수 있다.
             supabaseClient.auth.sessionStatus.collect { status ->
                 when(status) {
                     is SessionStatus.Authenticated -> _loginState.value = LoginState.LoggedIn
                     is SessionStatus.NotAuthenticated -> _loginState.value = LoginState.LoggedOut
                     else -> _loginState.value = LoginState.Unknown // Loading, NetworkError 등
                 }
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

    // 로그인 함수
    fun loginUser() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            try {
                // Supabase 로그인 로직
                supabaseClient.auth.signInWith(Email) {
                    email = _email.value
                    password = _password.value
                }
                _authUiState.value = AuthUiState.Success("로그인 성공!")
                _loginState.value = LoginState.LoggedIn // 로그인 성공 시 상태 변경
            } catch (e: Exception) {
                val errorMessage = when (e.message) {
                    null, "" -> "로그인 중 알 수 없는 에러가 발생했습니다."
                    "Invalid login credentials" -> "이메일 또는 비밀번호가 올바르지 않습니다."
                    // 필요한 경우 다른 특정 에러 메시지 케이스 추가
                    else -> e.message // 기본적으로 Supabase 메시지 사용
                }
                _authUiState.value = AuthUiState.Error(errorMessage!!)
                _loginState.value = LoginState.LoggedOut // 로그인 실패 시 상태 명시
            }
        }
    }

    // 회원가입 함수
    fun signUpUser() {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            if (_password.value != _confirmPassword.value) {
                _authUiState.value = AuthUiState.Error("비밀번호가 일치하지 않습니다.")
                return@launch
            }
            try {
                // Supabase 회원가입 로직
                val session = supabaseClient.auth.signUpWith(Email) {
                    email = _email.value
                    password = _password.value
                    data = buildJsonObject { put("display_name", JsonPrimitive(_nickname.value)) }
                }
                _authUiState.value = AuthUiState.Success("회원가입 성공! 확인 이메일을 확인해주세요.")
            } catch (e: Exception) {
                val errorMessage = when (e.message) {
                    null, "" -> "회원가입 중 알 수 없는 에러가 발생했습니다."
                    "User already registered" -> "이미 가입된 이메일입니다."
                    else -> e.message // 기본적으로 Supabase 메시지 사용
                }
                _authUiState.value = AuthUiState.Error(errorMessage!!)
            }
        }
    }

    // 로그아웃 함수
    fun logoutUser() {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
                _loginState.value = LoginState.LoggedOut
                _email.value = "" // 입력 필드 초기화
                _password.value = ""
                _confirmPassword.value = ""
                _authUiState.value = AuthUiState.Idle // UI 상태 초기화
            } catch (e: Exception) {
                // 로그아웃 실패 처리 (거의 발생하지 않지만)
                _authUiState.value = AuthUiState.Error(e.message ?: "로그아웃 중 에러가 발생했습니다.")
            }
        }
    }

    // UI 상태 초기화 함수
    fun resetAuthUiState() {
        _authUiState.value = AuthUiState.Idle
    }
} 