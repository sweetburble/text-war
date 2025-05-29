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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.presentation.viewmodels.character.CharacterListViewModel
import com.bandi.textwar.ui.theme.TextWarTheme
import timber.log.Timber

@Composable
fun CharacterListScreen(
    navController: NavController,
    onLogoutClick: () -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel() // ViewModel 주입
) {
    val characters by viewModel.characters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val opponentCharacter by viewModel.opponentCharacter.collectAsState()
    val isFindingOpponent by viewModel.isFindingOpponent.collectAsState()

    // 상대 찾기 성공 시 BattleResultScreen으로 이동 (opponentCharacter가 null이 아니게 되면 실행)
    opponentCharacter?.let {
        // BattleResultScreen 구현 후 실제 이동 로직으로 대체
        Timber.d("상대 찾음: ${it.characterName}, ID: ${it.id}. BattleResultScreen으로 이동합니다.")
        navController.navigate("battle_result/${it.id}") {
            // 이전 화면으로 돌아오지 않도록 설정할 수 있음
            // popUpTo("character_list") { inclusive = true }
        }
        viewModel.clearOpponent() // 이동 후 상대 정보 초기화
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
                } else if (error != null) {
                    Timber.e(error.toString())
                    Text(
                        text = error ?: "알 수 없는 오류가 발생했습니다.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (characters.isEmpty()) {
                    Text("생성된 캐릭터가 없습니다.", modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f), // 남은 공간을 모두 차지하도록
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(characters) { character ->
                            CharacterItem(
                                character = character,
                                onBattleClick = {
                                    viewModel.findOpponent()
                                },
                                onDetailClick = {
                                    navController.navigate("character_detail/${character.id}")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f)) // 버튼들을 하단에 가깝게 배치하기 위한 Spacer

                Button(
                    onClick = { navController.navigate("create_character") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("새 캐릭터 생성")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("로그아웃")
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
    onBattleClick: () -> Unit, 
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Card 전체를 클릭하면 상세 화면으로 이동
        // onClick = onDetailClick // 기존 onClick 제거
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
                    Text("상세보기")
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Button(onClick = onBattleClick) {
                    Text("배틀 시작")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterListScreenPreview_Empty() {
    TextWarTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("캐릭터 목록", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("생성된 캐릭터가 없습니다.")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) { Text("새 캐릭터 생성") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) { Text("로그아웃") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterListScreenPreview_WithData() {
    TextWarTheme {
        val navController = rememberNavController()
        val sampleCharacters = listOf(
            CharacterSummary(id = "1", characterName = "용감한 기사", description = "정의를 위해 싸우는 용감한 기사입니다."),
            CharacterSummary(id = "2", characterName = "신비로운 마법사", description = "고대 마법을 사용하는 신비로운 존재입니다.")
        )
        // ViewModel의 실제 동작을 모방하기 어려우므로, UI 구조만 확인
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("캐릭터 목록", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(sampleCharacters) { character ->
                        CharacterItem(
                            character = character, 
                            onBattleClick = {}, 
                            onDetailClick = {}
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(0.1f))
                Button(onClick = { navController.navigate("create_character") }, modifier = Modifier.fillMaxWidth()) { Text("새 캐릭터 생성") }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* onLogoutClick */ }, modifier = Modifier.fillMaxWidth()) { Text("로그아웃") }
            }
        }
    }
}