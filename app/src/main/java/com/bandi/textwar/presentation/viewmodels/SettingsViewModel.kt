package com.bandi.textwar.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.bandi.textwar.presentation.viewmodels.state.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Settings 화면의 상태와 이벤트를 관리하는 ViewModel
 * - 로그아웃, 회원탈퇴 요청 등 UI 이벤트 처리
 * - AuthViewModel의 상태를 구독하여 UI 업데이트 (선택적)
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // UseCases 직접 주입 제거
) : ViewModel() {

    // Settings 화면 자체의 UI 상태 (예: 로딩 스피너, 버튼 활성화 여부 등)
    private val _settingsScreenUiState = MutableStateFlow(SettingsScreenUiState())
    val settingsScreenUiState: StateFlow<SettingsScreenUiState> = _settingsScreenUiState.asStateFlow()

    // AuthViewModel의 AuthUiState를 구독하여 로딩/에러 상태를 반영할 수 있습니다.
    // 이 부분은 SettingsScreen에서 직접 AuthViewModel의 authUiState를 관찰하는 것으로 대체할 수도 있습니다.
    // 여기서는 SettingsViewModel이 AuthViewModel의 상태 변화에 따라 자체 UI를 업데이트하는 예시를 보여줍니다.
    fun observeAuthUiState(authUiState: AuthUiState) {
        when (authUiState) {
            is AuthUiState.Loading -> {
                _settingsScreenUiState.value = _settingsScreenUiState.value.copy(isLoading = true, message = null)
            }
            is AuthUiState.Success -> {
                _settingsScreenUiState.value = _settingsScreenUiState.value.copy(isLoading = false, message = authUiState.message)
            }
            is AuthUiState.Error -> {
                _settingsScreenUiState.value = _settingsScreenUiState.value.copy(isLoading = false, message = authUiState.message)
            }
            is AuthUiState.Idle -> {
                _settingsScreenUiState.value = _settingsScreenUiState.value.copy(isLoading = false, message = null)
            }
        }
    }

    // 메시지 표시 후 초기화
    fun clearMessage() {
        _settingsScreenUiState.value = _settingsScreenUiState.value.copy(message = null)
    }

    // 아래 logout, withdraw 함수는 이제 실제 로직을 수행하지 않고,
    // UI에서 이 함수들이 호출되면, 상위 Composable(MainActivity)에 정의된 콜백을 실행하도록 합니다.
    // SettingsViewModel은 버튼 클릭 시 로딩 상태를 true로 변경하는 등의 역할만 할 수 있습니다.

    /**
     * 로그아웃 요청 시 UI 상태 변경 (실제 로그아웃은 콜백으로 처리)
     */
    fun onLogoutRequest() {
        // 필요시 로딩 상태 변경 등 UI 로직 처리
        // _settingsScreenUiState.value = _settingsScreenUiState.value.copy(isLoading = true)
        // 실제 로그아웃 처리는 MainActivity에 있는 authViewModel.logoutUser()가 호출되도록 함
    }

    /**
     * 회원탈퇴 요청 시 UI 상태 변경 (실제 회원탈퇴는 콜백으로 처리)
     */
    fun onWithdrawRequest() {
        // 필요시 로딩 상태 변경 등 UI 로직 처리
        // _settingsScreenUiState.value = _settingsScreenUiState.value.copy(isLoading = true)
        // 실제 회원탈퇴 처리는 MainActivity에 있는 authViewModel.withdrawUser()가 호출되도록 함
    }
}

/**
 * Settings 화면 자체의 UI 상태 데이터 클래스
 */
data class SettingsScreenUiState(
    val isLoading: Boolean = false, // 로딩 여부 (SettingsScreen 자체의 로딩)
    val message: String? = null,    // 성공/에러 메시지 (AuthViewModel의 상태를 반영)
    val withdrawSuccess: Boolean = false // 이 필드는 AuthViewModel의 AuthUiState.Success 메시지로 대체 가능
    // AuthUiState 및 LoginState 제거
)