package com.bandi.textwar.presentation.viewmodels.state

// 인증 상태를 나타내는 sealed interface
sealed interface AuthUiState {
    object Idle : AuthUiState // 초기 상태
    object Loading : AuthUiState // 로딩 중 상태
    data class Success(val message: String) : AuthUiState // 성공 상태
    data class Error(val message: String) : AuthUiState // 에러 상태
}