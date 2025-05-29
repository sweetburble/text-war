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
                        AuthNavigation()
                    }
                }
            }
        }
    }
}