package com.bandi.textwar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bandi.textwar.ui.screens.auth.LoginScreen
import com.bandi.textwar.ui.screens.auth.SignUpScreen

object AuthDestinations {
    const val LOGIN_ROUTE = "login"
    const val SIGN_UP_ROUTE = "signup"
}

@Composable
fun AuthNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AuthDestinations.LOGIN_ROUTE) {
        composable(AuthDestinations.LOGIN_ROUTE) {
            // LoginScreen으로 ViewModel을 넘길 필요 없이 LoginScreen 내부에서 viewModel() 호출
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(AuthDestinations.SIGN_UP_ROUTE) },
                onLoginSuccess = {} // 로그인 성공 콜백 전달
            )
        }
        composable(AuthDestinations.SIGN_UP_ROUTE) {
            // SignUpScreen으로 ViewModel을 넘길 필요 없이 SignUpScreen 내부에서 viewModel() 호출
            SignUpScreen(
                onNavigateToLogin = { navController.navigate(AuthDestinations.LOGIN_ROUTE) {popUpTo(AuthDestinations.LOGIN_ROUTE) { inclusive = true } } },
                onSignUpSuccess = {
                    // 회원가입 성공 후 로그인 화면으로 이동하고, 백스택에서 회원가입 화면 제거
                    navController.navigate(AuthDestinations.LOGIN_ROUTE) {
                        popUpTo(AuthDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }
} 