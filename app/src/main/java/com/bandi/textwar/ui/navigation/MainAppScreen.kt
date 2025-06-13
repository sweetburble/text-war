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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.bandi.textwar.presentation.viewmodels.AuthViewModel
import com.bandi.textwar.presentation.viewmodels.shared.SharedEventViewModel
import com.bandi.textwar.ui.screens.battle.BattleDetailScreen
import com.bandi.textwar.ui.screens.battle.BattleHistoryScreen
import com.bandi.textwar.ui.screens.battle.BattleResultScreen
import com.bandi.textwar.ui.screens.character.CharacterCreationScreen
import com.bandi.textwar.ui.screens.character.CharacterDetailScreen
import com.bandi.textwar.ui.screens.character.CharacterListScreen
import com.bandi.textwar.ui.screens.leaderboard.LeaderboardScreen
import com.bandi.textwar.ui.screens.settings.SettingsScreen

@Composable
fun MainAppScreen(
    // MainAppScreen 내부에서 사용할 NavController. MainActivity의 NavController와는 다르다
    internalNavController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
) {
    // Activity 범위의 SharedEventViewModel 생성 (ViewModelStoreOwner를 명시적으로 지정)
    val sharedEventViewModel: SharedEventViewModel = hiltViewModel(LocalContext.current as ViewModelStoreOwner)

    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // 하단 네비게이션 바를 표시해야 하는 라우트 목록
    val bottomBarVisibleRoutes = listOf(
        BottomNavItem.CharacterList.route,
        BottomNavItem.BattleHistory.route, // 인자가 없는 BattleHistoryScreen 경로
        BottomNavItem.Leaderboard.route,
        BottomNavItem.AppSettings.route
    )

    val shouldShowBottomBar = currentRoute in bottomBarVisibleRoutes

    Scaffold(
        topBar = {
            MainTopAppBar(navController = internalNavController) // 내부 NavController 사용
        },
        bottomBar = {
            if (shouldShowBottomBar) { // 조건부로 하단 네비게이션 바 표시
                MainBottomNavigationBar(navController = internalNavController) // 내부 NavController 사용
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) } // 스낵바 호스트 설정
    ) { innerPadding ->
        MainNavigationGraph(
            navController = internalNavController, // 내부 NavController 사용
            authViewModel = authViewModel, // AuthViewModel 전달
            sharedEventViewModel = sharedEventViewModel,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun MainTopAppBar(navController: NavHostController) { // 파라미터 타입 NavHostController로 유지
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute = currentDestination?.route
    val title = remember(currentRoute) {
        // 기존 title 로직 유지
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
            else -> "Text War" // 기본 타이틀
        }
    }

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            // BottomNavItem에 해당하지 않는 화면이고, 이전 백스택이 있을 때만 뒤로가기 아이콘 표시
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
fun MainBottomNavigationBar(navController: NavHostController) { // 파라미터 타입 NavHostController로 유지
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
    navController: NavHostController, // 내부 NavController
    authViewModel: AuthViewModel,
    sharedEventViewModel: SharedEventViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.CharacterList.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.CharacterList.route) {
            CharacterListScreen(navController = navController)
        }
        composable(BottomNavItem.BattleHistory.route) {
            BattleHistoryScreen(navController = navController)
        }
        composable(CharacterBattleHistoryNav.routeWithArgName(), arguments = CharacterBattleHistoryNav.arguments) {
            BattleHistoryScreen(navController = navController)
        }
        composable(BottomNavItem.Leaderboard.route) {
            LeaderboardScreen(navController = navController, sharedEventViewModel = sharedEventViewModel)
        }
        composable(BottomNavItem.AppSettings.route) {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel,
            )
        }

        composable(CreateCharacterNav.route) {
            CharacterCreationScreen(
                navController = navController,
                sharedEventViewModel = sharedEventViewModel,
                onSaveSuccessNavigation = {
                    navController.popBackStack()
                }
            )
        }

        composable(CharacterDetailNav.routeWithArgName(), arguments = CharacterDetailNav.arguments) {
            CharacterDetailScreen(navController = navController, sharedEventViewModel = sharedEventViewModel)
        }

        composable(BattleResultNav.routeWithArgNames(), arguments = BattleResultNav.arguments) {
            BattleResultScreen(navController = navController)
        }

        composable(BattleDetailNav.routeWithArgName(), arguments = BattleDetailNav.arguments) {
            BattleDetailScreen(navController = navController)
        }
    }
}