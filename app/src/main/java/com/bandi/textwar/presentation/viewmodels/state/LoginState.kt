package com.bandi.textwar.presentation.viewmodels.state

// 로그인 상태를 나타내는 sealed interface
sealed interface LoginState {
    object Unknown : LoginState      // 초기 알 수 없는 상태 또는 확인 중
    object LoggedIn : LoginState     // 로그인 된 상태
    object LoggedOut : LoginState    // 로그아웃 된 상태
}