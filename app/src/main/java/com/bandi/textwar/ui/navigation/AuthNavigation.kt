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
fun AuthNavigation(onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()
    // AuthViewModel은 LoginScreen과 SignUpScreen 내부에서 각각 viewModel()로 호출되므로
    // 여기서 별도로 생성하거나 전달할 필요는 일반적으로 없습니다.
    // 만약 최상위 AuthNavigation에서 AuthViewModel의 loginState를 직접 관찰해야 한다면
    // 여기서 hiltViewModel<AuthViewModel>() 로 가져와서 LaunchedEffect 등으로 처리할 수 있습니다.

    NavHost(navController = navController, startDestination = AuthDestinations.LOGIN_ROUTE) {
        composable(AuthDestinations.LOGIN_ROUTE) {
            // LoginScreen으로 ViewModel을 넘길 필요 없이 LoginScreen 내부에서 viewModel() 호출
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(AuthDestinations.SIGN_UP_ROUTE) },
                onLoginSuccess = onLoginSuccess // 로그인 성공 콜백 전달
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
                    // 여기서는 onLoginSuccess를 직접 호출하지 않고, 로그인 화면에서 실제 로그인을 통해 성공 콜백이 호출되도록 유도
                }
            )
        }
    }
} 