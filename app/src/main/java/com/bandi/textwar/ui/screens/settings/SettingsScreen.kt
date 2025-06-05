package com.bandi.textwar.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * 설정 화면 - UI만 구현, 로그아웃/회원탈퇴만 실제 동작
 */
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.SettingsViewModel
import com.bandi.textwar.presentation.viewmodels.state.AuthUiState


// 임시 showToast 함수 (실제로는 유틸리티 함수로 분리하는 것이 좋음)
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * SettingsScreen - ViewModel과 연결된 Jetpack Compose 화면
 */
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onWithdraw: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // AuthViewModel의 UI 상태 관찰 (로딩, 성공, 에러 등)
    val authUiState by authViewModel.authUiState.collectAsState()

    // SettingsViewModel의 UI 상태 관찰 (예: 다이얼로그 표시 여부 등)
    // val settingsScreenState by settingsViewModel.screenState.collectAsState() // 필요시 사용

    // AuthUiState에 따른 UI 처리 (주로 로딩 인디케이터)
    // 성공/에러 메시지는 MainActivity의 Snackbar에서 처리됨
    val isLoading = authUiState is AuthUiState.Loading

    // 회원탈퇴 성공 시의 네비게이션은 MainActivity의 LoginState 변경 감지를 통해 자동으로 처리됩니다.
    // 따라서 여기서 별도의 네비게이션 로직이나 토스트는 필요 없습니다.
    // AuthUiState.Success 메시지가 MainActivity의 스낵바로 표시됩니다.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // 테마 색상 사용
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 프로필 영역
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .clickable { showToast(context, "프로필 관리 준비중") },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), // 테마 색상 사용
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "프로필",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // 테마 색상 사용
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "개인 정보",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground // 테마 색상 사용
                )
                Text(
                    "user123", // 실제 사용자 정보로 대체 필요
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // 테마 색상 사용
                    fontSize = 14.sp
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = "프로필 이동",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant) // 테마 색상 및 두께 조정

        // 계정 관리
        SettingItem(
            title = "계정 관리",
            imageVector = Icons.Default.ManageAccounts,
            onClick = { showToast(context, "계정 관리 준비중") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant) // 구분선 색상 변경

        // 게임 안내/정보
        SettingItem(
            title = "게임 안내",
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            onClick = { showToast(context, "게임 안내 준비중") }
        )
        SettingItem(
            title = "게임 정보",
            imageVector = Icons.Default.Info,
            onClick = { showToast(context, "게임 정보 준비중") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)

        // 기본 설정
        SettingItem(
            title = "언어 선택",
            imageVector = Icons.Default.Language,
            value = "한국어",
            onClick = { showToast(context, "언어 변경 준비중") }
        )
        SettingItem(
            title = "알림 설정",
            imageVector = Icons.Default.Notifications,
            onClick = { showToast(context, "알림 설정 준비중") }
        )
        SettingItem(
            title = "앱 설정",
            imageVector = Icons.Default.Settings,
            onClick = { showToast(context, "앱 설정 준비중") }
        )
        Spacer(modifier = Modifier.weight(1f)) // 하단 버튼들을 화면 아래로 밀어냄

        // 로그아웃 버튼
        Button(
            onClick = {
                // settingsViewModel.showConfirmDialog(DialogType.LOGOUT) // 확인 다이얼로그 표시 (선택적)
                onLogout() // 전달받은 콜백 실행 -> AuthViewModel.logoutUser() 호출
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = !isLoading // 로딩 중이 아닐 때만 활성화
        ) {
            Text("로그아웃")
        }

        // 회원탈퇴 버튼
        OutlinedButton(
            onClick = {
                // settingsViewModel.showConfirmDialog(DialogType.WITHDRAW) // 확인 다이얼로그 표시 (선택적)
                onWithdraw() // 전달받은 콜백 실행 -> AuthViewModel.withdrawUser() 호출
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(1.dp, if (!isLoading) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)), // 로딩 시 비활성화 스타일
            enabled = !isLoading // 로딩 중이 아닐 때만 활성화
        ) {
            Text("회원 탈퇴")
        }

        // 로딩 인디케이터 (화면 중앙 하단 또는 버튼 위)
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        }
    }

    // 예시: 확인 다이얼로그 (SettingsViewModel과 연동)
    // if (settingsScreenState.showDialog) {
    //     AlertDialog(
    //         onDismissRequest = { settingsViewModel.dismissDialog() },
    //         title = { Text(if (settingsScreenState.dialogType == DialogType.LOGOUT) "로그아웃" else "회원탈퇴") },
    //         text = { Text(if (settingsScreenState.dialogType == DialogType.LOGOUT) "정말 로그아웃 하시겠습니까?" else "정말 회원탈퇴 하시겠습니까?\n모든 데이터가 삭제되며 복구할 수 없습니다.") },
    //         confirmButton = {
    //             Button(
    //                 onClick = {
    //                     if (settingsScreenState.dialogType == DialogType.LOGOUT) onLogout() else onWithdraw()
    //                     settingsViewModel.dismissDialog()
    //                 }
    //             ) { Text("확인") }
    //         },
    //         dismissButton = {
    //             Button(onClick = { settingsViewModel.dismissDialog() }) { Text("취소") }
    //         }
    //     )
    // }
}

/**
 * 단일 설정 항목(아이콘 + 제목 + 우측 화살표)
 */
@Composable
fun SettingItem(
    title: String,
    imageVector: ImageVector,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector,
            contentDescription = title,
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, modifier = Modifier.weight(1f))
        value?.let {
            Text(it, color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}