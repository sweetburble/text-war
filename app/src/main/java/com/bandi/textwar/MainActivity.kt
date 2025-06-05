package com.bandi.textwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState // 스낵바 사용을 위해 추가
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // 스낵바 상태를 위해 추가
import androidx.compose.runtime.rememberCoroutineScope // 스낵바 스코프를 위해 추가
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.state.AuthUiState
import com.bandi.textwar.presentation.viewmodels.state.LoginState
import com.bandi.textwar.ui.navigation.LoginNav
import com.bandi.textwar.ui.navigation.MainAppScreen
import com.bandi.textwar.ui.navigation.NavigationRouteName
import com.bandi.textwar.ui.navigation.SignUpNav
import com.bandi.textwar.ui.screens.LoadingScreen
import com.bandi.textwar.ui.screens.auth.LoginScreen
import com.bandi.textwar.ui.screens.auth.SignUpScreen
import com.bandi.textwar.ui.theme.TextWarTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextWarTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val authUiState by authViewModel.authUiState.collectAsState()
                LaunchedEffect(authUiState) {
                    when (val state = authUiState) { // 명시적 타입 캐스팅을 위해 변수 할당
                        is AuthUiState.Success -> {
                            if (state.message.isNotBlank()) { // 메시지가 있을 때만 표시
                                scope.launch {
                                    snackbarHostState.showSnackbar(state.message)
                                }
                                authViewModel.resetAuthUiState()
                            }
                        }
                        is AuthUiState.Error -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(state.message)
                            }
                            authViewModel.resetAuthUiState()
                        }
                        else -> Unit
                    }
                }

                AppNavigation(
                    authViewModel = authViewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel, snackbarHostState: SnackbarHostState) {
    val appNavController = rememberNavController() // MainActivity의 NavController
    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState, appNavController) {
        when (loginState) {
            LoginState.LoggedIn -> {
                if (appNavController.currentDestination?.route?.startsWith(NavigationRouteName.MAIN_GRAPH) == false) {
                    appNavController.navigate(NavigationRouteName.MAIN_GRAPH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            LoginState.LoggedOut -> {
                if (appNavController.currentDestination?.route?.startsWith(NavigationRouteName.AUTH_GRAPH) == false) {
                    appNavController.navigate(NavigationRouteName.AUTH_GRAPH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            LoginState.Unknown -> {
                // Unknown 상태일 때, 명시적으로 로딩 화면으로 보내거나 AUTH_GRAPH로 보낼 수 있습니다.
                // 현재 startDestination 로직과 중복될 수 있으므로 주의.
                // 예: 초기 로딩 중이거나 아직 그래프가 결정되지 않았을 때
                if (appNavController.currentDestination?.route != NavigationRouteName.LOADING_SCREEN &&
                    appNavController.currentDestination?.route?.startsWith(NavigationRouteName.AUTH_GRAPH) == false &&
                    appNavController.currentDestination?.route?.startsWith(NavigationRouteName.MAIN_GRAPH) == false
                ) {
                    // 주석 처리: startDestination이 Unknown일 때 AUTH_GRAPH로 가므로,
                    // checkCurrentUserSession이 완료될 때까지 AUTH_GRAPH의 LoginScreen이 보이거나,
                    // LoginScreen에서 로딩 상태를 표시할 수 있습니다.
                    // 또는 명시적 로딩 화면을 원한다면 아래 주석 해제
                    // appNavController.navigate(NavigationRouteName.LOADING_SCREEN) { popUpTo(0) { inclusive = true } }
                }
            }
        }
    }

    NavHost(
        navController = appNavController,
        startDestination = if (loginState == LoginState.LoggedIn) NavigationRouteName.MAIN_GRAPH
        else NavigationRouteName.AUTH_GRAPH // Unknown 포함, 초기 세션 체크 전까지 AUTH_GRAPH
    ) {
        navigation(
            route = NavigationRouteName.AUTH_GRAPH,
            startDestination = LoginNav.route
        ) {
            composable(LoginNav.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToSignUp = { appNavController.navigate(SignUpNav.route) }
                )
            }
            composable(SignUpNav.route) {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { appNavController.popBackStack() },
                    onSignUpSuccess = {
                        appNavController.navigate(LoginNav.route) {
                            popUpTo(LoginNav.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // MAIN_GRAPH를 composable로 직접 정의하고, MainAppScreen이 자체 NavHost를 갖도록 합니다.
        composable(route = NavigationRouteName.MAIN_GRAPH) {
            // MainAppScreen은 자체 NavController를 사용합니다.
            // MainActivity의 appNavController는 MainAppScreen으로의 진입/이탈만 관리합니다.
            MainAppScreen(
                // MainAppScreen 내부 NavController는 MainAppScreen에서 rememberNavController()로 생성
                authViewModel = authViewModel, // AuthViewModel 전달
                snackbarHostState = snackbarHostState, // SnackbarHostState 전달
                onLogout = { // 로그아웃 콜백 전달
                    authViewModel.logoutUser()
                },
                onWithdraw = { // 회원탈퇴 콜백 전달
                    authViewModel.withdrawUser()
                }
            )
        }

        composable(NavigationRouteName.LOADING_SCREEN) {
            LoadingScreen()
        }
    }
}