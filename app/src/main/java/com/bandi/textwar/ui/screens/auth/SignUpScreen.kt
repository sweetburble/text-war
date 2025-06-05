package com.bandi.textwar.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel = hiltViewModel(), // ViewModel 주입
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit // 회원가입 성공 시 호출될 콜백
) {
    // ViewModel로부터 State들을 수집
    val nicknameValue by authViewModel.nickname.collectAsState()
    val emailValue by authViewModel.email.collectAsState()
    val passwordValue by authViewModel.password.collectAsState()
    val confirmPasswordValue by authViewModel.confirmPassword.collectAsState()
    val authUiState by authViewModel.authUiState.collectAsState()

    // 스낵바를 위한 상태 및 스코프
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // authUiState에 따라 스낵바 표시, 콜백 호출 및 상태 초기화
    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                snackbarHostState.showSnackbar(
                    scope = scope,
                    message = "회원가입 성공! 로그인 해주세요." // 성공 메시지
                )
                onSignUpSuccess() // 성공 콜백 호출 (예: 로그인 화면으로 이동)
                authViewModel.resetAuthUiState() // 성공 후 상태 초기화
            }
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(
                    scope = scope,
                    message = state.message,
                    duration = SnackbarDuration.Long // 에러 메시지는 길게 표시
                )
                // authViewModel.resetAuthUiState() // 필요에 따라 에러 후 상태 초기화
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
            Text("Text War 참전 준비하기", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nicknameValue,
                onValueChange = { authViewModel.onNicknameChange(it) },
                label = { Text("유저 닉네임") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

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

            OutlinedTextField(
                value = confirmPasswordValue,
                onValueChange = { authViewModel.onConfirmPasswordChange(it) },
                label = { Text("비밀번호 확인") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { authViewModel.signUpUser() },
                enabled = authUiState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authUiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("회원가입")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("이미 계정이 있으신가요? 로그인")
            }
        }
    }
}