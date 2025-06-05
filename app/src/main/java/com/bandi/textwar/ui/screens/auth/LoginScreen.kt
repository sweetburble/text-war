package com.bandi.textwar.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.state.AuthUiState
import com.bandi.textwar.ui.utils.showSnackbar // 스낵바 유틸리티 임포트
import timber.log.Timber

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = hiltViewModel(), // ViewModel 주입
    onNavigateToSignUp: () -> Unit,
) {
    // ViewModel로부터 State들을 수집
    val emailValue by authViewModel.email.collectAsState()
    val passwordValue by authViewModel.password.collectAsState()
    val authUiState by authViewModel.authUiState.collectAsState()

    // 스낵바를 위한 상태 및 스코프
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // authUiState에 따라 스낵바 표시 및 상태 초기화
    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                snackbarHostState.showSnackbar(
                    scope = scope,
                    message = "로그인 성공!" // 성공 메시지 (필요에 따라 ViewModel에서 전달받도록 수정 가능)
                )
                authViewModel.resetAuthUiState() // 성공 후 상태 초기화
            }
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(
                    scope = scope,
                    message = "로그인에 실패했습니다..",
                    duration = SnackbarDuration.Short
                )
                Timber.e(state.message)
            }
            else -> {
                // AuthUiState.Idle, AuthUiState.Loading 시에는 특별한 동작 없음
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Scaffold의 패딩 적용
                .padding(16.dp), // 기존 패딩 유지
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Text War 바로 참전하기", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = emailValue,
                onValueChange = { authViewModel.onEmailChange(it) },
                label = { Text("이메일") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = passwordValue,
                onValueChange = { authViewModel.onPasswordChange(it) },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 로딩 상태 처리
            Button(
                onClick = { authViewModel.loginUser() },
                enabled = authUiState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authUiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("로그인")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToSignUp) {
                Text("계정이 없으신가요? 회원가입")
            }
        }
    }
}