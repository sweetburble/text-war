package com.bandi.textwar.ui.screens.auth

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bandi.textwar.ui.theme.TextWarTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.AuthUiState

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    TextWarTheme {
        SignUpScreen(
            onNavigateToLogin = {},
            onSignUpSuccess = {}
        )
    }
}

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

    // authUiState에 따라 회원가입 성공 시 콜백 호출
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Success) {
            onSignUpSuccess()
            authViewModel.resetAuthUiState() // 성공 후 상태 초기화
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Text War 참전 준비하기", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // 유저 닉네임 입력 필드
        OutlinedTextField(
            value = nicknameValue,
            onValueChange = { authViewModel.onNicknameChange(it) }, // ViewModel의 함수 호출
            label = { Text("유저 닉네임") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 이메일 입력 필드
        OutlinedTextField(
            value = emailValue,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 입력 필드
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

        // 비밀번호 확인 입력 필드
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

        // 에러 메시지 및 로딩 상태 처리 (authUiState 기반)
        when (authUiState) {
            is AuthUiState.Error -> {
                Log.e("Signup error", (authUiState as AuthUiState.Error).message)
                Text(
                    text = (authUiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            else -> {} // Idle, Loading, Success 시 추가적인 Spacer 불필요
        }

        // 회원가입 버튼
        Button(
            onClick = { authViewModel.signUpUser() }, // ViewModel의 회원가입 함수 호출
            enabled = authUiState !is AuthUiState.Loading, // 로딩 중이 아닐 때만 활성화
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authUiState is AuthUiState.Loading) {
                // 로딩 중일 때 인디케이터 표시
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("회원가입")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 로그인 화면으로 이동하는 텍스트 버튼
        TextButton(onClick = onNavigateToLogin) {
            Text("이미 계정이 있으신가요? 로그인")
        }
    }
}