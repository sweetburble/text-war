package com.bandi.textwar.ui.screens.character

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.presentation.viewmodels.character.CharacterListViewModel
import com.bandi.textwar.ui.navigation.BattleResultNav
import com.bandi.textwar.ui.navigation.CharacterDetailNav
import com.bandi.textwar.ui.navigation.CreateCharacterNav
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import timber.log.Timber
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.bandi.textwar.domain.usecases.battle.CheckBattleCooldownUseCase

@Composable
fun CharacterListScreen(
    navController: NavController,
    viewModel: CharacterListViewModel = hiltViewModel() // ViewModel 주입
) {
    // 화면에 진입할 때마다 캐릭터 목록을 새로 불러옵니다 (삭제/생성/수정 반영)
    LaunchedEffect(Unit) {
        viewModel.loadCharacters()
    }

    val characters by viewModel.characters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val opponentCharacter by viewModel.opponentCharacter.collectAsState()
    val isFindingOpponent by viewModel.isFindingOpponent.collectAsState()
    val myCharacterIdForBattle by viewModel.myCharacterIdForBattle.collectAsState() // 나의 캐릭터 ID 상태 관찰
    val characterCooldowns by viewModel.characterCooldowns.collectAsState() // 캐릭터별 쿨다운 상태 관찰

    // 오류 메시지 표시 로직 (쿨다운 메시지 포함)
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() }, // 외부 클릭 시 닫기
            title = { Text("알림") },
            text = { Text(error.toString()) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("확인")
                }
            }
        )
    }

    // 상대 찾기 성공 시 BattleResultScreen으로 이동
    // (opponentCharacter와 myCharacterIdForBattle가 모두 null이 아니게 되면 실행)
    if (opponentCharacter != null && myCharacterIdForBattle != null) {
        val currentOpponent = opponentCharacter
        val currentMyCharacterId = myCharacterIdForBattle
        Timber.d("나의 캐릭터 ID: $currentMyCharacterId, 상대 찾음: ${currentOpponent?.characterName}, ID: ${currentOpponent?.id}. BattleResultScreen으로 이동합니다.")
        navController.navigate(BattleResultNav.navigateWithArgs(currentMyCharacterId!!, currentOpponent!!.id)) {
            // popUpTo("character_list") { inclusive = true }
        }
        viewModel.clearBattleContext()
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("캐릭터 목록", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (characters.isEmpty()) {
                    Text("생성된 캐릭터가 없습니다.", modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f), // 남은 공간을 모두 차지하도록
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(characters, key = { it.id }) { character -> // key 추가 권장
                            val cooldownInfoFromViewModel = characterCooldowns[character.id] // Pair<Boolean, Long?>

                            // 화면에 표시될 남은 시간(초)과 쿨다운 상태
                            var displayRemainingTimeSec by remember { mutableLongStateOf(0L) }
                            var displayIsInCooldown by remember { mutableStateOf(false) }

                            LaunchedEffect(key1 = cooldownInfoFromViewModel) { // ViewModel의 정보가 바뀔 때마다 재계산
                                val lastBattleTimestampMillis = cooldownInfoFromViewModel?.second
                                if (lastBattleTimestampMillis != null) {
                                    val cooldownEndTimeMillis = lastBattleTimestampMillis + (CheckBattleCooldownUseCase.COOLDOWN_SECONDS * 1000L)

                                    // 현재 시간 기준으로 남은 시간 계산 및 상태 업데이트 (1초마다)
                                    while (isActive) { // CoroutineScope가 살아있는 동안 반복
                                        val currentTimeMillis = System.currentTimeMillis()
                                        val remainingMillis = (cooldownEndTimeMillis - currentTimeMillis).coerceAtLeast(0L)
                                        displayRemainingTimeSec = remainingMillis / 1000L
                                        displayIsInCooldown = remainingMillis > 0L

                                        if (!displayIsInCooldown) break // 쿨다운 종료 시 루프 탈출
                                        delay(1000L) // 1초 대기
                                    }
                                } else { // 쿨다운 정보가 없으면 (예: 아직 전투 안 함)
                                    displayIsInCooldown = false
                                    displayRemainingTimeSec = 0L
                                }
                            }

                            CharacterItem(
                                character = character,
                                // 화면 표시용 상태 전달
                                characterCooldownDisplay = Pair(displayIsInCooldown, displayRemainingTimeSec),
                                onBattleClick = {
                                    // 버튼 클릭 시에는 ViewModel의 findOpponent를 호출하여
                                    // ViewModel이 최신 DB 상태를 기준으로 쿨다운을 다시 한번 확인하도록 함
                                    if (!displayIsInCooldown) { // UI 상 즉각적인 피드백 (선택적)
                                        viewModel.findOpponent(character.id)
                                    } else {
                                        // 이미 쿨다운 중임을 알리는 Toast 등을 보여줄 수 있음
                                        // 혹은 그냥 viewModel.findOpponent(character.id)를 호출하여
                                        // ViewModel에서 에러 메시지를 띄우도록 해도 됨
                                        viewModel.findOpponent(character.id) // ViewModel에서 처리하도록 넘김
                                    }
                                },
                                onDetailClick = {
                                    navController.navigate(CharacterDetailNav.navigateWithArg(character.id))
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f)) // 버튼들을 하단에 가깝게 배치하기 위한 Spacer

                Button(
                    onClick = { 
                        navController.navigate(CreateCharacterNav.route) 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("새 캐릭터 생성")
                }
            }

            // 배틀 대기 로딩 화면
            if (isFindingOpponent) {
                Box(
                    modifier = Modifier.fillMaxSize().align(Alignment.Center).padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    AlertDialog(
                        onDismissRequest = { /* 로딩 중에는 닫을 수 없음 */ },
                        title = { Text("대결 상대 검색 중") },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("잠시만 기다려주세요...")
                            }
                        },
                        confirmButton = { }
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterItem(
    character: CharacterSummary,
    characterCooldownDisplay: Pair<Boolean, Long>, // 해당 캐릭터의 쿨다운 정보 (쿨다운 중인가, 남은 시간)
    onBattleClick: (myCharacterId: String) -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = character.characterName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = character.description, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            // 버튼들을 오른쪽 정렬하기 위한 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // 버튼들을 오른쪽으로 정렬
            ) {
                Button(onClick = onDetailClick) {
                    Text("상세보기", style = MaterialTheme.typography.labelLarge) // 스타일 적용 권장
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                val isInCooldown = characterCooldownDisplay.first
                val remainingCooldownTime = characterCooldownDisplay.second

                Button(
                    onClick = { onBattleClick(character.id) },
                    enabled = !isInCooldown // UI 상에서 즉시 비활성화 (ViewModel에서도 재확인)
                ) {
                    if (isInCooldown && remainingCooldownTime > 0L) {
                        Text("대기 (${remainingCooldownTime}초)", style = MaterialTheme.typography.labelLarge) // 스타일 적용 권장
                    } else {
                        Text("배틀 시작", style = MaterialTheme.typography.labelLarge) // 스타일 적용 권장
                    }
                }
            }
        }
    }
}
