package com.bandi.textwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.LoginState
import com.bandi.textwar.ui.navigation.AuthNavigation
import com.bandi.textwar.ui.screens.LoadingScreen
import com.bandi.textwar.ui.screens.MainAppScreen
import com.bandi.textwar.ui.theme.TextWarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextWarTheme {
                val loginState by authViewModel.loginState.collectAsState()

                when (loginState) {
                    LoginState.Unknown -> {
                        LoadingScreen()
                    }
                    LoginState.LoggedIn -> {
                        MainAppScreen(
                            onLogoutClick = { authViewModel.logoutUser() }
                        )
                    }
                    LoginState.LoggedOut -> {
                        AuthNavigation(
                            onLoginSuccess = {
                                // 이 콜백은 AuthNavigation -> LoginScreen을 통해 호출됨
                                // loginState가 AuthViewModel 내부에서 LoginState.LoggedIn으로 변경될 것이므로
                                // 이 when 문이 recomposition되어 MainAppScreen으로 자동 전환됨
                                // 특별한 액션이 필요하다면 여기에 추가
                            }
                        )
                    }
                }
            }
        }
    }
}