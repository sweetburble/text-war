package com.bandi.textwar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.bandi.textwar.ui.screens.character.CharacterCreationScreen
import com.bandi.textwar.ui.screens.character.CharacterDetailScreen
import com.bandi.textwar.ui.screens.character.CharacterListScreen
import com.bandi.textwar.ui.theme.TextWarTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TempBattleResultScreen(
    navController: NavController,
    opponentId: String?
) { // 임시 화면
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("배틀 결과 화면 (임시)", style = MaterialTheme.typography.headlineMedium)
        Text("상대 ID: $opponentId")
        Button(onClick = { navController.popBackStack() }) {
            Text("돌아가기")
        }
    }
}

@Composable
fun MainAppScreen(onLogoutClick: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "character_list") {
        composable("character_list") {
            CharacterListScreen(
                navController = navController,
                onLogoutClick = onLogoutClick
            )
        }
        composable("create_character") {
            CharacterCreationScreen(
                navController = navController,
                onSaveSuccessNavigation = {
                    // 캐릭터 생성 성공 후 캐릭터 목록 화면으로 이동, 백스택에서 캐릭터 생성 화면 제거
                    navController.navigate("character_list") {
                        popUpTo("character_list") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "character_detail/{characterId}",
            arguments = listOf(navArgument("characterId") { type = NavType.StringType })
        ) { backStackEntry ->
            CharacterDetailScreen(navController = navController)
        }
        composable(
            route = "battle_result/{opponentId}",
            arguments = listOf(navArgument("opponentId") { type = NavType.StringType })
        ) { backStackEntry ->
            // TODO: 임시 구현
            val opponentId = backStackEntry.arguments?.getString("opponentId")
            TempBattleResultScreen(navController = navController, opponentId = opponentId) // 임시 화면 연결
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