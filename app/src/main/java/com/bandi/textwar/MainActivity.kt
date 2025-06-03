package com.bandi.textwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.LoginState
import com.bandi.textwar.ui.navigation.LoginNav
import com.bandi.textwar.ui.navigation.MainAppScreen
import com.bandi.textwar.ui.navigation.NavigationRouteName
import com.bandi.textwar.ui.navigation.SignUpNav
import com.bandi.textwar.ui.screens.LoadingScreen
import com.bandi.textwar.ui.screens.auth.LoginScreen
import com.bandi.textwar.ui.screens.auth.SignUpScreen
import com.bandi.textwar.ui.theme.TextWarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextWarTheme {
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState, navController) {
        when (loginState) {
            LoginState.LoggedIn -> {
                navController.navigate(NavigationRouteName.MAIN_GRAPH) {
                    popUpTo(0) { inclusive = true }
                }
            }
            LoginState.LoggedOut -> {
                navController.navigate(NavigationRouteName.AUTH_GRAPH) {
                    popUpTo(0) { inclusive = true }
                }
            }
            LoginState.Unknown -> {
                // 초기 상태 또는 상태 리셋 시 아무것도 안 함 (또는 로딩 화면으로 유지)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (loginState == LoginState.LoggedIn) NavigationRouteName.MAIN_GRAPH
                         else NavigationRouteName.AUTH_GRAPH
    ) {
        navigation(
            route = NavigationRouteName.AUTH_GRAPH,
            startDestination = LoginNav.route
        ) {
            composable(LoginNav.route) {
                LoginScreen(
                    onNavigateToSignUp = { navController.navigate(SignUpNav.route) },
                    onLoginSuccess = {
                        // LoginScreen 내부의 ViewModel에서 로그인 성공 시 loginState가 LoggedIn으로 변경될 것이고,
                        // 위의 LaunchedEffect가 네비게이션을 처리
                        // authViewModel.checkLoginStatus() // MainActivity의 ViewModel을 통해 상태 갱신 요청
                    }
                )
            }
            composable(SignUpNav.route) {
                SignUpScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate(LoginNav.route) {
                            popUpTo(LoginNav.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(NavigationRouteName.MAIN_GRAPH) {
            MainAppScreen(
                onLogoutSuccess = {
                    // MainAppScreen의 SettingsScreen에서 로그아웃 버튼 클릭 시
                    // authViewModel.logoutUser()가 호출되어 loginState가 LoggedOut으로 변경되고,
                    // 위의 LaunchedEffect가 AUTH_GRAPH로 네비게이션 처리
                    authViewModel.logoutUser()
                }
            )
        }

        composable(NavigationRouteName.LOADING_SCREEN) {
            LoadingScreen()
        }
    }

    if (loginState == LoginState.Unknown && navController.currentDestination?.route != NavigationRouteName.LOADING_SCREEN) {
        // 이 로직은 startDestination과 LaunchedEffect의 타이밍 이슈를 해결하기 위해 추가될 수 있습니다.
        // LaunchedEffect보다 먼저 실행되어야 로딩화면이 먼저 보입니다.
        // 하지만 현재 구조에서는 startDestination을 AUTH_GRAPH로 두고 LaunchedEffect로 처리하는 것이 더 깔끔할 수 있습니다.
        // navController.navigate(NavigationRouteName.LOADING_SCREEN) { popUpTo(0){ inclusive = true }} // 주석 처리
    }
}