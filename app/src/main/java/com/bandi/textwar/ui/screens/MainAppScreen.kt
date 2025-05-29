package com.bandi.textwar.ui.screens

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
    }
}


@Preview(showBackground = true)
@Composable
fun MainAppScreenPreview() {
    TextWarTheme {
        MainAppScreen(onLogoutClick = {})
    }
}