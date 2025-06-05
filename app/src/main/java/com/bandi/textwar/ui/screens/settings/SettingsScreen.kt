package com.bandi.textwar.ui.screens.settings

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold 
import androidx.compose.material3.SnackbarDuration 
import androidx.compose.material3.SnackbarHost 
import androidx.compose.material3.SnackbarHostState 
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect 
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.SettingsViewModel
import com.bandi.textwar.presentation.viewmodels.state.AuthUiState
import com.bandi.textwar.ui.components.ConfirmActionDialog
import com.bandi.textwar.ui.utils.ToastUtils
import kotlinx.coroutines.launch 


/**
 * SettingsScreen - ViewModel과 연결된 Jetpack Compose 화면
 */
@Composable
fun SettingsScreen(
    navController: NavController, // MainAppScreen의 internalNavController
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authUiState by authViewModel.authUiState.collectAsState()
    val isLoading = authUiState is AuthUiState.Loading

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 로그아웃/회원탈퇴 성공 또는 실패 시 스낵바 표시 및 화면 전환
    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                if (state.message.contains("로그아웃") || state.message.contains("회원탈퇴")) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                    authViewModel.resetAuthUiState() // 상태 초기화
                }
            }

            is AuthUiState.Error -> {
                if (state.message.contains("로그아웃") || state.message.contains("회원탈퇴")) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.message,
                            duration = SnackbarDuration.Short,
                            withDismissAction = true
                        )
                    }
                    authViewModel.resetAuthUiState()
                }
            }

            else -> Unit
        }
    }

    // 로그아웃 확인 다이얼로그
    if (showLogoutDialog) {
        ConfirmActionDialog(
            onDismissRequest = { showLogoutDialog = false },
            onConfirmation = {
                showLogoutDialog = false
                authViewModel.logoutUser() // ViewModel의 로그아웃 함수 직접 호출
            },
            dialogTitle = "로그아웃",
            dialogText = "정말로 로그아웃 하시겠습니까?",
            confirmButtonText = "로그아웃"
        )
    }

    // 회원 탈퇴 확인 다이얼로그
    if (showWithdrawDialog) {
        ConfirmActionDialog(
            onDismissRequest = { showWithdrawDialog = false },
            onConfirmation = {
                showWithdrawDialog = false
                authViewModel.withdrawUser() // ViewModel 직접 호출
            },
            dialogTitle = "회원 탈퇴",
            dialogText = "정말로 회원 탈퇴 하시겠습니까?\n모든 데이터가 삭제되며 복구할 수 없습니다.",
            confirmButtonText = "회원 탈퇴",
            dismissButtonText = "취소"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold의 padding 적용
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 프로필 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { ToastUtils.showToast(context, "프로필 관리 준비중") },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "프로필",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "개인 정보",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "user123", // 실제 사용자 정보로 대체 필요
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = "프로필 이동",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 계정 관리
            SettingItem(
                title = "계정 관리",
                imageVector = Icons.Default.ManageAccounts,
                onClick = { ToastUtils.showToast(context, "계정 관리 준비중") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // 게임 안내/정보
            SettingItem(
                title = "게임 안내",
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                onClick = { ToastUtils.showToast(context, "게임 안내 준비중") }
            )
            SettingItem(
                title = "게임 정보",
                imageVector = Icons.Default.Info,
                onClick = { ToastUtils.showToast(context, "게임 정보 준비중") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // 기본 설정
            SettingItem(
                title = "언어 선택",
                imageVector = Icons.Default.Language,
                value = "한국어",
                onClick = { ToastUtils.showToast(context, "언어 변경 준비중") }
            )
            SettingItem(
                title = "알림 설정",
                imageVector = Icons.Default.Notifications,
                onClick = { ToastUtils.showToast(context, "알림 설정 준비중") }
            )
            SettingItem(
                title = "앱 설정",
                imageVector = Icons.Default.Settings,
                onClick = { ToastUtils.showToast(context, "앱 설정 준비중") }
            )
            Spacer(modifier = Modifier.weight(1f))

            // 로그아웃 버튼
            Button(
                onClick = {
                    showLogoutDialog = true // 로그아웃 다이얼로그 표시
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors( // OutlinedButtonColors 대신 buttonColors 사용 또는 테마에 맞게 조정
                    containerColor = Color(0xFFFF5722).copy(alpha = 0.1f), // 예시: 배경색 약하게
                    contentColor = Color(0xFFFF5722)
                ),
                enabled = !isLoading
            ) {
                Text("로그아웃")
            }

            // 회원 탈퇴 버튼
            OutlinedButton(
                onClick = {
                    showWithdrawDialog = true // 회원 탈퇴 다이얼로그 표시
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    1.dp,
                    if (!isLoading) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.12f
                    )
                ),
                enabled = !isLoading
            ) {
                Text("회원 탈퇴")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
            }
        }
    }
}

/**
 * 단일 설정 항목(아이콘 + 제목 + 우측 화살표)
 */
@Composable
fun SettingItem(
    title: String,
    imageVector: ImageVector,
    value: String? = null,
    onClick: () -> Unit,
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant, // 테마 색상 사용
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            title,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground // 테마 색상 사용
        )
        value?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // 테마 색상 사용
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant // 테마 색상 사용
        )
    }
}