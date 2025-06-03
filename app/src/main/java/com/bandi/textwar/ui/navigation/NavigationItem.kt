package com.bandi.textwar.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Face6
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink


// Destination 인터페이스
interface Destination {
    val route: String
    val title: String // TopAppBar 제목 등으로 사용
    val deepLinks: List<NavDeepLink>
}

// 인자를 받는 Destination 인터페이스
interface DestinationArg<T> : Destination {
    val argName: String
    val arguments: List<NamedNavArgument>

    fun routeWithArgName(): String = "$route/{$argName}"
    fun navigateWithArg(arg: T): String
    fun findArgument(navBackStackEntry: NavBackStackEntry): T?
}

// Bottom Navigation Item 정의
sealed class BottomNavItem(
    override val route: String,
    val icon: ImageVector,
    override val title: String // 화면 제목 또는 식별자로 사용
) : Destination {
    object CharacterList : BottomNavItem(
        route = NavigationRouteName.CHARACTER_LIST,
        icon = Icons.Default.Face6,
        title = NavigationTitle.CHARACTER_LIST
    )

    object BattleHistory : BottomNavItem(
        route = NavigationRouteName.BATTLE_HISTORY,
        icon = Icons.Default.HistoryEdu,
        title = NavigationTitle.BATTLE_HISTORY
    )

    object Leaderboard : BottomNavItem(
        route = NavigationRouteName.LEADERBOARD,
        icon = Icons.Default.Leaderboard,
        title = NavigationTitle.LEADERBOARD
    )

    object AppSettings : BottomNavItem(
        route = NavigationRouteName.APP_SETTINGS,
        icon = Icons.Filled.Settings,
        title = NavigationTitle.APP_SETTINGS
    )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = "${NavigationRouteName.DEEP_LINK_SCHEME}$route" }
    )

    companion object {
        fun isBottomNavRoute(route: String?): Boolean {
            return when (route) {
                NavigationRouteName.CHARACTER_LIST, NavigationRouteName.BATTLE_HISTORY, NavigationRouteName.LEADERBOARD, NavigationRouteName.APP_SETTINGS -> true
                else -> false
            }
        }
    }
}

// 기타 화면 Navigation 객체 (필요에 따라 추가)
object LoginNav : Destination {
    override val route: String = NavigationRouteName.LOGIN
    override val title: String = NavigationTitle.LOGIN
    override val deepLinks: List<NavDeepLink> = listOf(navDeepLink { uriPattern = "${NavigationRouteName.DEEP_LINK_SCHEME}$route" })
}

object SignUpNav : Destination {
    override val route: String = NavigationRouteName.SIGN_UP
    override val title: String = NavigationTitle.SIGN_UP
    override val deepLinks: List<NavDeepLink> = listOf(navDeepLink { uriPattern = "${NavigationRouteName.DEEP_LINK_SCHEME}$route" })
}

object CreateCharacterNav : Destination {
    override val route: String = NavigationRouteName.CREATE_CHARACTER
    override val title: String = NavigationTitle.CREATE_CHARACTER
    override val deepLinks: List<NavDeepLink> = listOf(navDeepLink { uriPattern = "${NavigationRouteName.DEEP_LINK_SCHEME}$route" })
}

// 캐릭터 상세 화면 Nav
object CharacterDetailNav : DestinationArg<String> {
    override val route: String = NavigationRouteName.CHARACTER_DETAIL
    override val title: String = NavigationTitle.CHARACTER_DETAIL
    override val argName: String = "characterId"
    override val deepLinks: List<NavDeepLink> = listOf(navDeepLink { uriPattern = "${NavigationRouteName.DEEP_LINK_SCHEME}$route/{$argName}" })
    override val arguments: List<NamedNavArgument> = listOf(
        androidx.navigation.navArgument(argName) { type = androidx.navigation.NavType.StringType }
    )
    override fun navigateWithArg(arg: String): String = "$route/$arg"
    override fun findArgument(navBackStackEntry: NavBackStackEntry): String? = navBackStackEntry.arguments?.getString(argName)
}

// 전투 결과 화면 Nav
object BattleResultNav : DestinationArg<Pair<String, String>> {
    override val route: String = NavigationRouteName.BATTLE_RESULT
    override val title: String = NavigationTitle.BATTLE_RESULT
    private const val MY_CHARACTER_ID_ARG = "myCharacterId"
    private const val OPPONENT_CHARACTER_ID_ARG = "opponentId"

    // DestinationArg 인터페이스의 argName은 대표 인자 이름으로 사용하고,
    // 실제 라우트 구성과 인자 추출은 내부적으로 처리합니다.
    override val argName: String = MY_CHARACTER_ID_ARG

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = "${NavigationRouteName.DEEP_LINK_SCHEME}$route/{$MY_CHARACTER_ID_ARG}/{$OPPONENT_CHARACTER_ID_ARG}" }
    )
    override val arguments: List<NamedNavArgument> = listOf(
        androidx.navigation.navArgument(MY_CHARACTER_ID_ARG) { type = androidx.navigation.NavType.StringType },
        androidx.navigation.navArgument(OPPONENT_CHARACTER_ID_ARG) { type = androidx.navigation.NavType.StringType }
    )

    override fun navigateWithArg(arg: Pair<String, String>): String {
        TODO("Not yet implemented")
    }

    // DestinationArg의 navigateWithArg는 단일 인자용이므로, 이 경우 별도 함수 사용 또는 수정 필요
    fun navigateWithArgs(myCharacterId: String, opponentId: String): String {
        return "$route/$myCharacterId/$opponentId"
    }

    // DestinationArg의 findArgument는 단일 인자용이므로, 이 경우 별도 함수 사용 또는 수정 필요
    override fun findArgument(navBackStackEntry: NavBackStackEntry): Pair<String, String>? {
        val myId = navBackStackEntry.arguments?.getString(MY_CHARACTER_ID_ARG)
        val opponentId = navBackStackEntry.arguments?.getString(OPPONENT_CHARACTER_ID_ARG)
        return if (myId != null && opponentId != null) {
            if (opponentId == "null") Pair(myId, "") // "null" 문자열은 빈 문자열로 처리하거나, null로 처리할지 결정
            else Pair(myId, opponentId)
        } else null
    }

    // NavController.navigate()에서 사용할 경로
    fun routeWithArgNames(): String = "$route/{$MY_CHARACTER_ID_ARG}/{$OPPONENT_CHARACTER_ID_ARG}"
}


// 캐릭터 ID를 받는 BattleHistory Nav
object CharacterBattleHistoryNav : DestinationArg<String> {
    // 기본 BattleHistory 라우트와 구분하기 위해 접미사 추가 또는 다른 이름 사용
    override val route: String = "${NavigationRouteName.BATTLE_HISTORY}_character"
    override val title: String = NavigationTitle.BATTLE_HISTORY_DETAIL
    override val argName: String = "characterId"
    override val deepLinks: List<NavDeepLink> = listOf(navDeepLink { uriPattern = "${NavigationRouteName.DEEP_LINK_SCHEME}$route/{$argName}" })
    override val arguments: List<NamedNavArgument> = listOf(
        androidx.navigation.navArgument(argName) { type = androidx.navigation.NavType.StringType }
    )
    override fun navigateWithArg(arg: String): String = "$route/$arg"
    override fun findArgument(navBackStackEntry: NavBackStackEntry): String? = navBackStackEntry.arguments?.getString(argName)
}

// Navigation Route Name 정의
object NavigationRouteName {
    const val DEEP_LINK_SCHEME = "textwar://" // 앱의 딥링크 스킴으로 변경

    const val LOADING_SCREEN = "loading_screen" // 로딩 화면

    // Bottom Navigation Routes
    const val CHARACTER_LIST = "character_list"
    const val BATTLE_HISTORY = "battle_history"
    const val LEADERBOARD = "leaderboard"
    const val APP_SETTINGS = "app_settings"

    // Other Routes (예시: 기존 화면들)
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val CREATE_CHARACTER = "create_character"
    const val CHARACTER_DETAIL = "character_detail"
    const val BATTLE_RESULT = "battle_result"

    // Auth Graph
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
}

// Navigation Title (TopAppBar 등에서 사용될 수 있음)
object NavigationTitle {
    const val CHARACTER_LIST = "내 캐릭터"
    const val BATTLE_HISTORY = "나의 전투 기록"
    const val LEADERBOARD = "리더보드"
    const val APP_SETTINGS = "앱 설정"

    const val LOGIN = "로그인"
    const val SIGN_UP = "회원가입"
    const val CREATE_CHARACTER = "캐릭터 생성"
    const val CHARACTER_DETAIL = "캐릭터 정보"
    const val BATTLE_HISTORY_DETAIL = "전투 기록"
    const val BATTLE_RESULT = "전투 결과"
}