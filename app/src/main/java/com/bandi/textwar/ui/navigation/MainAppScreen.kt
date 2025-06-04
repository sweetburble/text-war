@file:OptIn(ExperimentalMaterial3Api::class)

package com.bandi.textwar.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bandi.textwar.ui.screens.battle.BattleDetailScreen
import com.bandi.textwar.ui.screens.battle.BattleHistoryScreen
import com.bandi.textwar.ui.screens.battle.BattleResultScreen
import com.bandi.textwar.ui.screens.character.CharacterCreationScreen
import com.bandi.textwar.ui.screens.character.CharacterDetailScreen
import com.bandi.textwar.ui.screens.character.CharacterListScreen
import com.bandi.textwar.ui.screens.leaderboard.LeaderboardScreen
import com.bandi.textwar.ui.screens.settings.SettingsScreen
import com.bandi.textwar.presentation.viewmodels.shared.SharedEventViewModel

@Composable
fun MainAppScreen(
    navController: NavHostController = rememberNavController(),
    onLogoutSuccess: () -> Unit // 로그아웃 성공 시 호출될 콜백
) {
    // Activity 범위의 SharedEventViewModel 생성 (ViewModelStoreOwner를 명시적으로 지정)
    val sharedEventViewModel: SharedEventViewModel = hiltViewModel(LocalContext.current as ViewModelStoreOwner)

    Scaffold(
        topBar = {
            MainTopAppBar(navController = navController)
        },
        bottomBar = {
            MainBottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        MainNavigationGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onLogoutSuccess = onLogoutSuccess,
            sharedEventViewModel = sharedEventViewModel
        )
    }
}

@Composable
fun MainTopAppBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 현재 라우트를 기반으로 타이틀 결정
    val currentRoute = currentDestination?.route
    val title = remember(currentRoute) {
        when (currentRoute) {
            BottomNavItem.CharacterList.route -> BottomNavItem.CharacterList.title
            BottomNavItem.BattleHistory.route -> BottomNavItem.BattleHistory.title
            BottomNavItem.Leaderboard.route -> BottomNavItem.Leaderboard.title
            BottomNavItem.AppSettings.route -> BottomNavItem.AppSettings.title
            CreateCharacterNav.route -> CreateCharacterNav.title
            CharacterDetailNav.routeWithArgName() -> CharacterDetailNav.title
            BattleResultNav.routeWithArgNames() -> BattleResultNav.title
            CharacterBattleHistoryNav.routeWithArgName() -> CharacterBattleHistoryNav.title
            BattleDetailNav.routeWithArgName() -> BattleDetailNav.title
            else -> "Text War"
        }
    }

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            val isTopLevelDestination = BottomNavItem.isBottomNavRoute(currentRoute)
            if (!isTopLevelDestination && navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로 가기"
                    )
                }
            }
        }
    )
}

@Composable
fun MainBottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.CharacterList,
        BottomNavItem.BattleHistory,
        BottomNavItem.Leaderboard,
        BottomNavItem.AppSettings
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogoutSuccess: () -> Unit,
    sharedEventViewModel: SharedEventViewModel
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.CharacterList.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.CharacterList.route) {
            CharacterListScreen(navController = navController, sharedEventViewModel = sharedEventViewModel) // onLogoutClick은 SettingsScreen에서 처리
        }
        composable(BottomNavItem.BattleHistory.route) {
            BattleHistoryScreen(navController = navController)
        }
        composable(CharacterBattleHistoryNav.routeWithArgName(), arguments = CharacterBattleHistoryNav.arguments) {
            val characterId = CharacterBattleHistoryNav.findArgument(it)
            BattleHistoryScreen(navController = navController, characterId = characterId)
        }
        composable(BottomNavItem.Leaderboard.route) {
            LeaderboardScreen(navController = navController, sharedEventViewModel = sharedEventViewModel)
        }
        composable(BottomNavItem.AppSettings.route) {
            SettingsScreen(navController = navController, onLogoutClick = onLogoutSuccess)
        }

        // 캐릭터 생성 화면
        composable(CreateCharacterNav.route) {
            CharacterCreationScreen(
                navController = navController,
                sharedEventViewModel = sharedEventViewModel,
                onSaveSuccessNavigation = {
                    navController.popBackStack() // 저장 성공 시 이전 화면(CharacterListScreen)으로 돌아감
                }
            )
        }

        // 캐릭터 상세 화면
        composable(CharacterDetailNav.routeWithArgName(), arguments = CharacterDetailNav.arguments) {
            CharacterDetailScreen(navController = navController, sharedEventViewModel = sharedEventViewModel)
        }

        // 전투 결과 화면
        composable(BattleResultNav.routeWithArgNames(), arguments = BattleResultNav.arguments) {
            BattleResultScreen(navController = navController)
        }

        // 전투 상세 화면
        composable(BattleDetailNav.routeWithArgName(), arguments = BattleDetailNav.arguments) {
            // recordId는 ViewModel에서 SavedStateHandle을 통해 자동으로 주입받으므로, 여기서 명시적으로 넘길 필요 없음
            BattleDetailScreen(navController = navController)
        }
    }
} 