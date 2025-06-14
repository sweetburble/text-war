package com.bandi.textwar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextWarTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                AppNavigation(
                    authViewModel = authViewModel,
                    snackbarHostState = snackbarHostState,
                    appNavController = rememberNavController() // NavController를 여기서 생성하여 전달
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    appNavController: NavHostController // 파라미터로 받음
) {
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
            LoginState.Unknown -> {}
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

        // MAIN_GRAPH를 composable로 직접 정의하고, MainAppScreen이 자체 NavHost를 갖도록 한다.
        composable(route = NavigationRouteName.MAIN_GRAPH) {
            // MainAppScreen은 자체 NavController를 사용
            // MainActivity의 appNavController는 MainAppScreen으로의 진입/이탈만 관리
            MainAppScreen(
                // MainAppScreen 내부 NavController는 MainAppScreen에서 rememberNavController()로 생성
                authViewModel = authViewModel, // AuthViewModel 전달
                snackbarHostState = snackbarHostState, // SnackbarHostState 전달
            )
        }

        composable(NavigationRouteName.LOADING_SCREEN) {
            LoadingScreen()
        }
    }
}