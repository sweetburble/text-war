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
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue

@Composable
fun CharacterListScreen(
    navController: NavController,
    viewModel: CharacterListViewModel = hiltViewModel() // ViewModel 주입
) {
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
                        items(characters) { character ->
                            // 각 캐릭터 아이템에 대해 쿨다운 정보 가져오기
                            val currentCooldownInfo = characterCooldowns[character.id]
                            var remainingTime by remember(character.id, currentCooldownInfo?.second) { mutableLongStateOf(currentCooldownInfo?.second ?: 0L) }
                            var isInCooldown by remember(character.id, currentCooldownInfo?.first) { mutableStateOf(currentCooldownInfo?.first == true) }

                            // LaunchedEffect의 key에 character.id와 currentCooldownInfo의 내부 값들을 직접 사용하여
                            // 아이템이 재사용되거나 ViewModel의 정보가 변경될 때마다 Effect가 올바르게 재시작되도록 함
                            LaunchedEffect(key1 = character.id, key2 = currentCooldownInfo?.first, key3 = currentCooldownInfo?.second) {
                                isInCooldown = currentCooldownInfo?.first == true
                                remainingTime = currentCooldownInfo?.second ?: 0L
                                if (isInCooldown && remainingTime > 0) {
                                    launch {
                                        while (isActive && remainingTime > 0) {
                                            delay(1000L)
                                            remainingTime = (remainingTime - 1).coerceAtLeast(0L)
                                            if (remainingTime == 0L) {
                                                isInCooldown = false
                                            }
                                        }
                                    }
                                }
                            }

                            CharacterItem(
                                character = character,
                                characterCooldown = Pair(isInCooldown, remainingTime),
                                onBattleClick = {
                                    if (!isInCooldown) { // UI 단에서 한번 더 체크
                                        viewModel.findOpponent(character.id)
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
    characterCooldown: Pair<Boolean, Long>?, // 해당 캐릭터의 쿨다운 정보 (쿨다운 중인가, 남은 시간)
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
                    Text("상세보기", fontWeight = FontWeight.Thin)
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))

                val isInCooldown = characterCooldown?.first == true
                val remainingCooldownTime = characterCooldown?.second ?: 0L

                Button(
                    onClick = { onBattleClick(character.id) },
                    enabled = !isInCooldown // 쿨다운 중이 아닐 때만 활성화
                ) {
                    if (isInCooldown && remainingCooldownTime > 0L) {
                        Text("대기 (${remainingCooldownTime}초)")
                    } else {
                        Text("배틀 시작")
                    }
                }
            }
        }
    }
}
