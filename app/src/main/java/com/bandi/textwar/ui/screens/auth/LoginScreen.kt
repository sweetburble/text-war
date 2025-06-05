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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.state.AuthUiState
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

    // authUiState에 따라 로그인 성공 시 콜백 호출
    // LaunchedEffect를 사용하여 Composable의 생명주기와 관계없이 특정 상태 변화에 반응
    androidx.compose.runtime.LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Success) {
            authViewModel.resetAuthUiState() // 성공 후 상태 초기화
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Text War 바로 참전하기", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = emailValue,
            onValueChange = { authViewModel.onEmailChange(it) }, // ViewModel의 함수 호출
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordValue,
            onValueChange = { authViewModel.onPasswordChange(it) }, // ViewModel의 함수 호출
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 에러 메시지 및 로딩 상태 처리 (authUiState 기반)
        when (authUiState) {
            is AuthUiState.Error -> {
                Timber.e((authUiState as AuthUiState.Error).message)
                Text(
                    text = (authUiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            else -> {} // Idle, Loading, Success 시 추가적인 Spacer 불필요
        }

        Button(
            onClick = { authViewModel.loginUser() }, // ViewModel의 로그인 함수 호출
            enabled = authUiState !is AuthUiState.Loading, // 로딩 중이 아닐 때만 활성화
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