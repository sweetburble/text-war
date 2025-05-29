package com.bandi.textwar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.bandi.textwar.ui.screens.character.CharacterCreationScreen
import com.bandi.textwar.ui.theme.TextWarTheme

@Composable
fun MainAppScreen(onLogoutClick: () -> Unit) {
    val navController = rememberNavController() // 임시

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("메인 앱 화면", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = {
                    navController.navigate("create_character")
                }) {
                    Text("캐릭터 생성 화면 이동")
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onLogoutClick) {
                    Text("로그아웃")
                }
                // TODO: 실제 앱 콘텐츠 구현
            }
        }
        composable("create_character") {
            CharacterCreationScreen(
                onSaveSuccessNavigation = {
                    // 캐릭터 생성 성공 후 메인 화면으로 이동, 백스택에서 회원가입 화면 제거
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    TextWarTheme {
        MainAppScreen(onLogoutClick = {})
    }
} 